package com.imatalk.chatservice.service;


import com.imatalk.chatservice.dto.response.CommonResponse;
import com.imatalk.chatservice.dto.response.NotificationDTO;
import com.imatalk.chatservice.entity.Notification;
import com.imatalk.chatservice.mongoRepository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;


    public void save(Notification notification) {
        notificationRepository.save(notification);
    }

    public ResponseEntity<CommonResponse> getNotifications(String currentUserId) {
        List<Notification> allByUserIdOrderByCreatedAtDesc = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(currentUserId);
        List<NotificationDTO> notificationDTOList = NotificationDTO.from(allByUserIdOrderByCreatedAtDesc);
        CommonResponse commonResponse = CommonResponse.success("Get notifications successfully", notificationDTOList);
        return ResponseEntity.ok(commonResponse);
    }


    public ResponseEntity<CommonResponse> seeAllNotifications(String currentUserId) {
        List<Notification> allUnreadNotifications = notificationRepository.findAllByUserIdAndUnread(currentUserId, true);

        for (Notification notification : allUnreadNotifications) {
            notification.setUnread(false);
        }
        notificationRepository.saveAll(allUnreadNotifications);

        return ResponseEntity.ok(CommonResponse.success("See all notifications successfully"));

    }
}
