package com.meilong.messenger.service;

import com.meilong.messenger.dto.TargetRequest;
import com.meilong.messenger.model.Target;
import com.meilong.messenger.repository.TargetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TargetService {
    private final TargetRepository targetRepository;

    public TargetService(TargetRepository targetRepository) {
        this.targetRepository = targetRepository;
    }

    @Transactional
    public Target create(TargetRequest request) {
        String url = CheckService.validateUrl(request.url());
        if (targetRepository.existsByUrl(url)) {
            throw new DuplicateTargetException(url);
        }
        return targetRepository.save(new Target(url));
    }

    @Transactional(readOnly = true)
    public List<Target> findAll() {
        return targetRepository.findAll();
    }

    @Transactional
    public void delete(long id) {
        if (!targetRepository.existsById(id)) {
            throw new TargetNotFoundException(id);
        }
        targetRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Target get(long id) {
        return targetRepository.findById(id).orElseThrow(() -> new TargetNotFoundException(id));
    }

    public static class TargetNotFoundException extends RuntimeException {
        public TargetNotFoundException(long id) {
            super("Target " + id + " was not found");
        }
    }

    public static class DuplicateTargetException extends RuntimeException {
        public DuplicateTargetException(String url) {
            super("Target already exists: " + url);
        }
    }
}
