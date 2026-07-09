package com.ltqtest.springbootquickstart.expert.repository;

import com.ltqtest.springbootquickstart.expert.entity.ExpertAppointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpertAppointmentRepository extends JpaRepository<ExpertAppointment, Long> {
    List<ExpertAppointment> findByUserId(Integer userId);

    
    Page<ExpertAppointment> findByExpertIdAndStatus(Integer expertId, String status, Pageable pageable);
    
    List<ExpertAppointment> findByExpertIdAndDateAndStatusIn(Integer expertId, LocalDate date, List<String> statusList);
    
    List<ExpertAppointment> findByExpertIdAndStatusIn(Integer expertId, List<String> statusList);

    List<ExpertAppointment> findByExpertIdAndDate(Integer expertId, LocalDate date);
    Optional<ExpertAppointment> findByExpertIdAndUserIdAndDate(Integer expertId, Integer userId, LocalDate date);
    Optional<ExpertAppointment> findByAppointmentId(Long appointmentId);
}
