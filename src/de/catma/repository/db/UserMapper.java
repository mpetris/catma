package de.catma.repository.db;

import static de.catma.repository.db.jooq.catmarepository.Tables.USER;

import org.jooq.Record;
import org.jooq.RecordMapper;

import de.catma.repository.db.model.DBUser;
import de.catma.user.Role;

public class UserMapper implements RecordMapper<Record, DBUser> {

	public DBUser map(Record record) {
		return new DBUser(
			record.getValue(USER.USERID),
			record.getValue(USER.IDENTIFIER),
			record.getValue(USER.LOCKED, Boolean.class),
			Role.getRole(record.getValue(USER.ROLE)));
	}

}
