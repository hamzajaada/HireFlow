-- Donnees de demonstration chargees au demarrage (base H2 en memoire).
-- offer_id et candidat_email renvoient aux donnees de service-offres et service-talents.
INSERT INTO application (offer_id, candidat_email, cv_url, lettre_motivation, date, statut) VALUES (1, 'amine@email.com', 'https://cv.example.com/amine.pdf', 'Motive par le poste Java.', '2026-10-20', 'EN_ENTRETIEN');
INSERT INTO application (offer_id, candidat_email, cv_url, lettre_motivation, date, statut) VALUES (2, 'sara@email.com', 'https://cv.example.com/sara.pdf', 'Experience Angular de 5 ans.', '2026-10-22', 'DEPOSEE');
INSERT INTO application (offer_id, candidat_email, cv_url, lettre_motivation, date, statut) VALUES (1, 'youssef@email.com', 'https://cv.example.com/youssef.pdf', 'Jeune diplome.', '2026-10-23', 'DEPOSEE');

INSERT INTO interview (application_id, date, evaluateur, note, avis) VALUES (1, '2026-10-28', 'RH - Mme Idrissi', NULL, NULL);
