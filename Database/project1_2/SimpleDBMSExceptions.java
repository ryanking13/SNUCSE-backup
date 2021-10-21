@SuppressWarnings("serial")
class SimpleDBMSException extends Exception {
  SimpleDBMSException(String msg) {
    super(msg);
  }
}

@SuppressWarnings("serial")
class DuplicateColumnDefError extends SimpleDBMSException {
  DuplicateColumnDefError() {
    super("Create table has failed: column definition is duplicated");
  }
}

@SuppressWarnings("serial")
class DuplicatePrimaryKeyDefError extends SimpleDBMSException {
  DuplicatePrimaryKeyDefError() {
    super("Create table has failed: primary key definition is duplicated");
  }
}

@SuppressWarnings("serial")
class ReferenceTypeError extends SimpleDBMSException {
  ReferenceTypeError() {
    super("Create table has failed: foreign key references wrong type");
  }
}

@SuppressWarnings("serial")
class ReferenceNonPrimaryKeyError extends SimpleDBMSException {
  ReferenceNonPrimaryKeyError() {
    super("Create table has failed: foreign key references non primary key column");
  }
}

@SuppressWarnings("serial")
class ReferenceColumnExistenceError extends SimpleDBMSException {
  ReferenceColumnExistenceError() {
    super("Create table has failed: foreign key references non existing column");
  }
}

@SuppressWarnings("serial")
class ReferenceTableExistenceError extends SimpleDBMSException {
  ReferenceTableExistenceError() {
    super("Create table has failed: foreign key references non existing table");
  }
}

@SuppressWarnings("serial")
class NonExistingColumnDefError extends SimpleDBMSException {
  NonExistingColumnDefError(String colName) {
    super(String.format("Create table has failed: '%s' does not exists in column definition", colName));
  }
}

@SuppressWarnings("serial")
class TableExistenceError extends SimpleDBMSException {
  TableExistenceError() {
    super("Create table has failed: table with the same name already exists");
  }
}

@SuppressWarnings("serial")
class DropReferencedTableError extends SimpleDBMSException {
  DropReferencedTableError(String tableName) {
    super(String.format("Drop table has failed: '%s' is referenced by other table", tableName));
  }
}

@SuppressWarnings("serial")
class ShowTablesNoTable extends SimpleDBMSException {
  ShowTablesNoTable() {
    super("There is no table");
  }
}

@SuppressWarnings("serial")
class NoSuchTable extends SimpleDBMSException {
  NoSuchTable() {
    super("No such table");
  }
}

@SuppressWarnings("serial")
class CharLengthError extends SimpleDBMSException {
  CharLengthError() {
    super("Char length should be over 0");
  }
}