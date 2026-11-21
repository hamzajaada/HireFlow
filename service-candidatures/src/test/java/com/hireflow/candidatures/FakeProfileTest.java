package com.hireflow.candidatures;

import com.hireflow.candidatures.application.client.OffreClient;
import com.hireflow.candidatures.application.client.OffreClientFake;
import com.hireflow.candidatures.interview.client.TalentClient;
import com.hireflow.candidatures.interview.client.TalentClientFake;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** Avec le profil "fake" : les clients Feign sont remplacés par les fichiers JSON de src/main/resources/fake/. */
@SpringBootTest
@ActiveProfiles("fake")
class FakeProfileTest {

    @Autowired OffreClient offreClient;
    @Autowired TalentClient talentClient;

    @Test
    void lesFakesRepondent() {
        assertThat(offreClient).isInstanceOf(OffreClientFake.class);
        assertThat(offreClient.verifier(1L, null).eligible()).isTrue();
        assertThat(offreClient.verifier(3L, null).statut()).isEqualTo("POURVUE");

        assertThat(talentClient).isInstanceOf(TalentClientFake.class);
        assertThat(talentClient.profil("amine@email.com").dernierScore()).isEqualTo(78);
        assertThat(talentClient.profil("inconnu@email.com")).isNull();
    }
}
