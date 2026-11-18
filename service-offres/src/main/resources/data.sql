-- Donnees de demonstration chargees au demarrage (base H2 en memoire).
INSERT INTO job_offer (intitule, departement, salaire_max, date_publication, statut, nombre_entretiens)
VALUES ('Developpeur Java Spring', 'IT', 18000, '2026-10-01', 'OUVERTE', 0);
INSERT INTO job_offer (intitule, departement, salaire_max, date_publication, statut, nombre_entretiens)
VALUES ('Developpeur Angular', 'IT', 15000, '2026-10-05', 'OUVERTE', 0);
INSERT INTO job_offer (intitule, departement, salaire_max, date_publication, statut, nombre_entretiens)
VALUES ('Comptable', 'Finance', 12000, '2026-09-15', 'POURVUE', 0);
INSERT INTO required_skill (offer_id, nom, niveau_minimum) VALUES (1, 'Java', 4);
INSERT INTO required_skill (offer_id, nom, niveau_minimum) VALUES (1, 'Spring Boot', 3);
INSERT INTO required_skill (offer_id, nom, niveau_minimum) VALUES (2, 'Angular', 3);
INSERT INTO required_skill (offer_id, nom, niveau_minimum) VALUES (2, 'TypeScript', 3);
INSERT INTO required_skill (offer_id, nom, niveau_minimum) VALUES (3, 'Comptabilite generale', 4);