package com.knu.coment;

import com.knu.coment.entity.FcmToken;
import com.knu.coment.repository.FcmTokenRepository;
import com.knu.coment.service.FcmService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class FcmServiceTest {

    @Mock
    private FcmTokenRepository fcmTokenRepository;

    @InjectMocks
    private FcmService fcmService;

    @Test
    void saveOrUpdateFcmToken_whenTokenExists_thenUpdateUserId() {
        // given
        Long userId = 1L;
        String fcmToken = "existingToken";
        FcmToken existing = new FcmToken(999L, fcmToken);

        given(fcmTokenRepository.findByFcmToken(fcmToken)).willReturn(Optional.of(existing));

        // when
        fcmService.saveOrUpdateFcmToken(userId, fcmToken);

        // then
        assertThat(existing.getUserId()).isEqualTo(userId);
        Mockito.verify(fcmTokenRepository, never()).save(any());
    }

    @Test
    void saveOrUpdateFcmToken_whenTokenNew_thenSaveNewToken() {
        Long userId = 1L;
        String fcmToken = "newToken";

        given(fcmTokenRepository.findByFcmToken(fcmToken)).willReturn(Optional.empty());

        fcmService.saveOrUpdateFcmToken(userId, fcmToken);

        Mockito.verify(fcmTokenRepository).save(argThat(token ->
                token.getUserId().equals(userId) &&
                        token.getFcmToken().equals(fcmToken)
        ));
    }
}

