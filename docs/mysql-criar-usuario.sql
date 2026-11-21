-- Rode UMA vez no MySQL Workbench, conectado como root (ou outro usuário administrador).
-- Cria o usuário local que a aplicação usa por padrão (perfil producao e teste de integração).
CREATE USER IF NOT EXISTS 'biblioteca'@'localhost' IDENTIFIED BY 'biblioteca';
GRANT ALL PRIVILEGES ON biblioteca.* TO 'biblioteca'@'localhost';
GRANT ALL PRIVILEGES ON biblioteca_teste.* TO 'biblioteca'@'localhost';
FLUSH PRIVILEGES;
