package com.hireflow.candidatures;

import com.hireflow.candidatures.application.client.OffreClient;
import com.hireflow.candidatures.application.client.OffreClientFake;
import com.hireflow.candidatures.interview.client.TalentClient;
import com.hireflow.candidatures.interview.client.TalentClientFake;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/** Sans profil : ce sont les vrais clients Feign (intégration). Aucun conflit avec les fakes. */
@SpringBootTest
class RealFeignTest {

    @Autowired OffreClient offreClient;
    @Autowired TalentClient talentClient;

    @Test
    void lesVraisClientsFeignSontUtilises() {
        assertThat(offreClient).isNotInstanceOf(OffreClientFake.class);
        assertThat(talentClient).isNotInstanceOf(TalentClientFake.class);
    }
}
