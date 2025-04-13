package am.shavigh.dbmigration.dto;

public class MigrationResult {

    private int rowsEffected;
    private String errorMessage;
    private String successMessage;

    public int getRowsEffected() {
        return rowsEffected;
    }

    public void setRowsEffected(int rowsEffected) {
        this.rowsEffected = rowsEffected;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }
}
