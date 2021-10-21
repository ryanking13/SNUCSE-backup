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

@SuppressWarnings("serial")
class InsertDuplicatePrimaryKeyError extends SimpleDBMSException {
  InsertDuplicatePrimaryKeyError() {
    super("Insertion has failed: Primary key duplication");
  }
}

@SuppressWarnings("serial")
class InsertReferentialIntegrityError extends SimpleDBMSException {
  InsertReferentialIntegrityError() {
    super("Insertion has failed: Referential integrity violation");
  }
}

@SuppressWarnings("serial")
class InsertTypeMismatchError extends SimpleDBMSException {
  InsertTypeMismatchError() {
    super("Insertion has failed: Types are not matched");
  }
}

@SuppressWarnings("serial")
class InsertColumnExistenceError extends SimpleDBMSException {
  InsertColumnExistenceError(String colName) {
    super(String.format("Insertion has failed: '%s' does not exist", colName));
  }
}

@SuppressWarnings("serial")
class InsertColumnNonNullableError extends SimpleDBMSException {
  InsertColumnNonNullableError(String colName) {
    super(String.format("Insertion has failed: '%s' is not nullable", colName));
  }
}

@SuppressWarnings("serial")
class SelectTableExistenceError extends SimpleDBMSException {
  SelectTableExistenceError(String tableName) {
    super(String.format("Selection has failed: '%s' does not exist", tableName));
  }
}

@SuppressWarnings("serial")
class SelectColumnResolveError extends SimpleDBMSException {
  SelectColumnResolveError(String colName) {
    super(String.format("Selection has failed: fail to resolve '%s'", colName));
  }
}

@SuppressWarnings("serial")
class WhereIncomparableError extends SimpleDBMSException {
  WhereIncomparableError() {
    super("Where clause try to compare incomparable values");
  }
}

@SuppressWarnings("serial")
class WhereTableNotSpecified extends SimpleDBMSException {
  WhereTableNotSpecified() {
    super("Where clause try to reference tables which are not specified");
  }
}

@SuppressWarnings("serial")
class WhereColumnNotExist extends SimpleDBMSException {
  WhereColumnNotExist() {
    super("Where clause try to reference non existing column");
  }
}

@SuppressWarnings("serial")
class WhereAmbiguousReference extends SimpleDBMSException {
  WhereAmbiguousReference() {
    super("Where clause contains ambiguous reference");
  }
}