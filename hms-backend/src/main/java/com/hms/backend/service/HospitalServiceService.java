package com.hms.backend.service;

import com.hms.backend.dto.PriceChangeRequest;
import com.hms.backend.dto.PriceHistoryDto;
import com.hms.backend.dto.ServiceCreationRequest;
import com.hms.backend.dto.ServiceDto;
import com.hms.backend.exception.DuplicateServiceException;
import com.hms.backend.model.HospitalService;
import com.hms.backend.model.ServicePriceHistory;
import com.hms.backend.repository.HospitalServiceRepository;
import com.hms.backend.repository.ServicePriceHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HospitalServiceService {

    private final HospitalServiceRepository serviceRepository;
    private final ServicePriceHistoryRepository historyRepository;

    public HospitalServiceService(HospitalServiceRepository serviceRepository,
            ServicePriceHistoryRepository historyRepository) {
        this.serviceRepository = serviceRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public ServiceDto createService(ServiceCreationRequest request, String createdBy) {
        // Rule: No duplicate service names per category
        if (serviceRepository.existsByServiceNameIgnoreCaseAndCategory(request.getServiceName(),
                request.getCategory())) {
            throw new DuplicateServiceException(
                    "A service named '" + request.getServiceName() + "' already exists in this category.");
        }

        HospitalService service = new HospitalService();
        service.setServiceId("SVC-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        service.setServiceName(request.getServiceName());
        service.setCategory(request.getCategory());
        service.setDefaultPrice(request.getDefaultPrice());
        service.setUnit(request.getUnit());
        service.setCreatedBy(createdBy);
        service.setActive(true);

        HospitalService saved = serviceRepository.save(service);

        // Record initial price in history
        logPriceChange(saved, BigDecimal.ZERO, saved.getDefaultPrice(), createdBy, "Initial Service Creation");

        return new ServiceDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ServiceDto> getAllServices(boolean activeOnly) {
        List<HospitalService> services = activeOnly
                ? serviceRepository.findByIsActiveTrueOrderByCategoryAscServiceNameAsc()
                : serviceRepository.findAllByOrderByCategoryAscServiceNameAsc();
        return services.stream().map(ServiceDto::new).collect(Collectors.toList());
    }

    @Transactional
    public ServiceDto updatePrice(String serviceId, PriceChangeRequest request, String username) {
        HospitalService service = serviceRepository.findByServiceId(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        if (service.getDefaultPrice().compareTo(request.getNewPrice()) == 0) {
            throw new IllegalArgumentException("New price is exactly the same as the current price.");
        }

        BigDecimal oldPrice = service.getDefaultPrice();
        service.setDefaultPrice(request.getNewPrice());
        HospitalService saved = serviceRepository.save(service);

        // Mandatory logging of price change
        logPriceChange(saved, oldPrice, request.getNewPrice(), username, request.getReason());

        return new ServiceDto(saved);
    }

    @Transactional
    public ServiceDto toggleActiveStatus(String serviceId, String username) {
        HospitalService service = serviceRepository.findByServiceId(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        service.setActive(!service.isActive());
        return new ServiceDto(serviceRepository.save(service));
    }

    @Transactional(readOnly = true)
    public List<PriceHistoryDto> getPriceHistory(String serviceId) {
        HospitalService service = serviceRepository.findByServiceId(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));

        return historyRepository.findByServiceOrderByChangedAtDesc(service).stream()
                .map(PriceHistoryDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PriceHistoryDto> getAllGlobalPriceHistory() {
        return historyRepository.findAllByOrderByChangedAtDesc().stream()
                .map(PriceHistoryDto::new)
                .collect(Collectors.toList());
    }

    private void logPriceChange(HospitalService service, BigDecimal oldPrice, BigDecimal newPrice, String changedBy,
            String reason) {
        ServicePriceHistory history = new ServicePriceHistory();
        history.setService(service);
        history.setOldPrice(oldPrice);
        history.setNewPrice(newPrice);
        history.setChangedBy(changedBy);
        history.setReason(reason);
        historyRepository.save(history);
    }
}
