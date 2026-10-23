package com.hireflow.candidatures.application.entity;

/**
 * Cycle de vie d'une candidature : DEPOSEE -> EN_ENTRETIEN -> RETENUE ou REFUSEE.
 * Le statut n'avance que dans un sens.
 */
public enum ApplicationStatus {
    DEPOSEE, EN_ENTRETIEN, RETENUE, REFUSEE
}
