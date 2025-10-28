package com.jungwook.lit_api.image.service;

import com.jungwook.lit_api.chat.domain.ChatRoom;
import com.jungwook.lit_api.chat.repository.ChatRoomRepository;
import com.jungwook.lit_api.image.domain.Metadata;
import com.jungwook.lit_api.image.repository.MetadataRepository;
import com.jungwook.lit_api.member.domain.Member;
import com.jungwook.lit_api.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.nio.file.AccessDeniedException;
import java.text.Normalizer;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {

    @Value("${aws.bucket}")
    private String bucketName;

    private final S3Presigner s3Presigner;
    private final MetadataRepository metadataRepository;

    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;


    public FileService(S3Presigner s3Presigner, MetadataRepository metadataRepository, MemberRepository memberRepository, ChatRoomRepository chatRoomRepository) {
        this.s3Presigner = s3Presigner;
        this.metadataRepository = metadataRepository;
        this.memberRepository = memberRepository;
        this.chatRoomRepository = chatRoomRepository;
    }

    public String generatePresignedUrl(String filePath, SdkHttpMethod method, UUID chatRoomId) {
        if (method == SdkHttpMethod.GET) {
            return generateGetPresignedUrl(chatRoomId);
        } else if (method == SdkHttpMethod.PUT) {
            return generatePutPreSignedUrl(filePath, chatRoomId);
        } else {
            throw new UnsupportedOperationException("Unsupported HTTP method: " + method);
        }
    }

    private String generateGetPresignedUrl(UUID chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new EntityNotFoundException("There is no such chat room."));

        Optional<Metadata> metadata = metadataRepository.findFirstByChatRoomOrderByCreatedTimeDesc(chatRoom);

        if(metadata.isEmpty()){
            return "/images/logo.png";
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(metadata.get().getName())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    private String generatePutPreSignedUrl(String filePath, UUID chatRoomId) {
        Member sender = memberRepository.findById(UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName()))
                .orElseThrow(()->new EntityNotFoundException("Member cannot be found"));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new EntityNotFoundException("There is no such chat room."));

        if(!chatRoom.getMember().equals(sender)){
            throw new IllegalArgumentException("You are not the owner of this room.");
        }

        PutObjectRequest.Builder putObjectRequestBuilder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(filePath);

        PutObjectRequest putObjectRequest = putObjectRequestBuilder.build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return presignedRequest.url().toString();
    }

    public void saveMetadata(String filename, UUID chatRoomId) {

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new EntityNotFoundException("There is no such chat room."));

        Metadata metadata = Metadata.builder()
                .name(filename)
                .chatRoom(chatRoom)
                .build();

        metadataRepository.save(metadata);
    }

    public static String buildFilename(String filename) {
        return String.format("%s_%s", System.currentTimeMillis(), sanitizeFileName(filename));
    }


    private static String sanitizeFileName(String fileName) {
        String normalizedFileName = Normalizer.normalize(fileName, Normalizer.Form.NFKD);
        return normalizedFileName.replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9.\\-_]", "");
    }
}
