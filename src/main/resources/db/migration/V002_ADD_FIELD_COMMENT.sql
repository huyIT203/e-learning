alter table comments
ADD COLUMN is_edited BOOLEAN DEFAULT FALSE COMMENT 'Flag to indicate if comment has been edited',
ADD COLUMN edited_at TIMESTAMP NULL COMMENT 'Timestamp when comment was last edited';

CREATE INDEX idx_comments_is_edited ON comments(is_edited);
CREATE INDEX idx_comments_edited_at ON comments(edited_at);
UPDATE comments SET is_edited = FALSE WHERE is_edited IS NULL;