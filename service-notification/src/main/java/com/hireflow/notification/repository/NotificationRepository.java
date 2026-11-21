package com.hireflow.notification.repository;

import com.hireflow.notification.entite.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByOrderByIdDesc();

    List<Notification> findByDestinataireIgnoreCaseOrderByIdDesc(String destinataire);
}
