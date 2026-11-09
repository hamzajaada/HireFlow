-- Donnees de demonstration chargees au demarrage (base H2 en memoire).
INSERT INTO candidate (nom, email, telephone, experience_annees, statut) VALUES ('Amine Jamali', 'amine@email.com', '0600000001', 3, 'ACTIF');
INSERT INTO candidate (nom, email, telephone, experience_annees, statut) VALUES ('Sara Bennani', 'sara@email.com', '0600000002', 5, 'ACTIF');
INSERT INTO candidate (nom, email, telephone, experience_annees, statut) VALUES ('Youssef Alaoui', 'youssef@email.com', '0600000003', 1, 'ACTIF');

INSERT INTO screening_test (candidate_id, test, score, date) VALUES (1, 'Java', 78, '2026-10-10');
INSERT INTO screening_test (candidate_id, test, score, date) VALUES (2, 'Angular', 85, '2026-10-12');
INSERT INTO screening_test (candidate_id, test, score, date) VALUES (3, 'Java', 55, '2026-10-14');
