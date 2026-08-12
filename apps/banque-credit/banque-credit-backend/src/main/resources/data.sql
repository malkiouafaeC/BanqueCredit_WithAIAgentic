-- Seed accounts (ASM-01, DEC-002/DEC-009). Mots de passe haches BCrypt.
-- conseiller1 / Conseiller123!
-- responsable1 / Responsable123!
-- Note : id delibrement omis (colonne IDENTITY) pour ne pas desynchroniser le compteur
-- auto-increment avec les insertions applicatives ulterieures (JPA).
MERGE INTO app_user (username, password, role, actif) KEY(username) VALUES
  ('conseiller1', '$2a$10$077QwLzhEqxvuOt9QHu5xuFfkxSIaCogv2uAQKjS7tbIM2Y/.THmq', 'CONSEILLER', TRUE),
  ('responsable1', '$2a$10$yOuIzl1LxE5BnNUP3J5YFOZzsaovR1FqJC1G9il7bxFcZMTs4MnXK', 'RESPONSABLE_CREDIT', TRUE);

