package com.creazione.space_learning.service;

import com.creazione.space_learning.config.DataSet;
import com.creazione.space_learning.dto.UserDto;
import com.creazione.space_learning.entities.UserEntity;
import com.creazione.space_learning.repository.UserRepository;
import com.creazione.space_learning.service.redis.UserCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserCacheService userCacheService;

    public Page<UserEntity> findUsersWithResourcesAndRecentBuildingUpdates(Pageable pageable) {
        return userRepository.findUsersWithResourcesAndRecentBuildingUpdates(pageable);
    }

    public UserEntity findFullUserByTelegramId(Long telegramId) {
        UserDto userDto = userCacheService.getUser(telegramId);
        if (userDto != null) {
            Long id = userDto.getId();
            userDto.setResources(DataSet.getResourceService().getResources(id, telegramId));
            userDto.setBuildings(DataSet.getBuildingService().getBuildings(id, telegramId));
            userDto.setBoosters(DataSet.getBoosterService().findAllIBByUserIdToList(id, telegramId));
            return userDto.convertToUserEntity();
        }

        Optional<UserEntity> userEntity = userRepository.findFullUserByTelegramId(telegramId);
        if (userEntity.isPresent()) {
            UserDto dto = userEntity.get().convertToUserDto();
            userCacheService.cacheFullUser(dto);
            return userEntity.get();
        } else {
            return null;
        }
    }

    public UserEntity saveFull(UserEntity userEntity) {
        UserEntity user = userRepository.save(userEntity);
        userCacheService.cacheFullUser(user.convertToUserDto());
        return user;
    }

    public UserEntity saveFullWithoutCache(UserEntity userEntity) {
        return userRepository.save(userEntity);
    }

    public UserEntity findById(Long userId) {
        Optional<UserEntity> userEntity = userRepository.findById(userId);
        return userEntity.orElse(null);
    }

    public Page<UserEntity> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<Long> findAllUserIds(Pageable pageable) {
        return userRepository.findAllUserIds(pageable);
    }

    public Page<UserEntity> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public UserEntity findUserWithResourcesById(Long userId) {
        Optional<UserEntity> userEntity = userRepository.findUserWithResourcesById(userId);
        return userEntity.orElse(null);
    }

    public void saveAll(List<UserEntity> userEntities) {
        for (UserEntity userEntity : userEntities) {
            userCacheService.deleteFullUser(userEntity.getTelegramId());
        }
        userRepository.saveAll(userEntities);
    }

    public int updateNameById(Long id, String name) {
        return userRepository.updateNameById(id, name);
    }

    public Optional<Long> findTelegramIdByUserId(Long userId) {
        return userRepository.findTelegramIdById(userId);
    }
}