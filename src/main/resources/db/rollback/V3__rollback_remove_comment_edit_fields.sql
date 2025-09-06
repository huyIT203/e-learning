DROP INDEX idx_comments_is_edited ON comments;
DROP INDEX idx_comments_edited_at ON comments;

ALTER TABLE comments
DROP COLUMN is_edited,
DROP COLUMN edited_at;