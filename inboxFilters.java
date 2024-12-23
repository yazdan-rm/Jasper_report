package ir.rbp.nabreport.dao.inbox.strategy;

import ir.rbp.nabcore.controller.exception.CustomParameterizeException;
import ir.rbp.nabcore.controller.exception.ErrorConstantsCore;
import ir.rbp.nabreport.common.enumeration.pvs.EN_OrgTypeLayerNum;
import ir.rbp.nabreport.common.enumeration.pvs.EN_UserRequestReportType;
import ir.rbp.nabreport.model.viewmodel.inbox.UserRequestReportVM;
import org.springframework.core.io.ClassPathResource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractUserRequestReportStrategy {

    public abstract EN_UserRequestReportType getReportType();

    public abstract ClassPathResource getJasperFile();

    public abstract List<UserRequestReportVM> generateData(StrategyContext strategyContext);


    public ClassPathResource getXlsxJasperFile() {
        String reportNumber = getJasperFile().getPath().replaceAll("\\D+", "");
        return new ClassPathResource("report/inbox/xlsx/" + reportNumber + "-X.jrxml");
    }

    protected String getColumnNameByOrgTypeLayerNum(Float orgTypeLayerNum) {
        return switch (EN_OrgTypeLayerNum.getById(orgTypeLayerNum)) {
            case SETAD, OSTAN -> " org.ostan_id";
            case NAHIYEH -> " org.nahiyeh_id";
            case MARKAZ -> " org.markaz_id";
            case HOZE -> " org.hozeh_id";
            case PAYGAH -> " org.paygah_id";
        };
    }

    protected String generateGroupByStatement(StrategyContext strategyContext) {
        return switch (EN_OrgTypeLayerNum.getById(strategyContext.getLayerNumOfSelectedOrgIds())) {
            case SETAD, OSTAN -> Boolean.TRUE.equals(strategyContext.getCalculateOrgChildren()) ? " org.ostan_id, org.nahiyeh_id\n" : " org.ostan_id\n";
            case NAHIYEH -> Boolean.TRUE.equals(strategyContext.getCalculateOrgChildren()) ? " org.nahiyeh_id, org.hozeh_id\n" : " org.nahiyeh_id\n";
            case MARKAZ -> Boolean.TRUE.equals(strategyContext.getCalculateOrgChildren()) ? " org.markaz_id, org.hozeh_id\n" : " org.markaz_id\n";
            case HOZE -> Boolean.TRUE.equals(strategyContext.getCalculateOrgChildren()) ? " org.hozeh_id, org.paygah_id\n" : " org.hozeh_id\n";
            default -> " org.paygah_id\n";
        };
    }

    protected String generateGroupedColumnForSelect(StrategyContext strategyContext) {
        return switch (EN_OrgTypeLayerNum.getById(strategyContext.getLayerNumOfSelectedOrgIds())) {
            case SETAD, OSTAN -> Boolean.TRUE.equals(strategyContext.getCalculateOrgChildren()) ? " min(org.ostan_title)     orgTitle,\n min(org.nahiyeh_title)      childOrgTitle,\n" : " min(org.ostan_title)       orgTitle,\n";
            case NAHIYEH -> Boolean.TRUE.equals(strategyContext.getCalculateOrgChildren()) ? " min(org.nahiyeh_title)   orgTitle,\n min(org.hozeh_title)        childOrgTitle,\n" : " min(org.nahiyeh_title)     orgTitle,\n";
            case MARKAZ -> Boolean.TRUE.equals(strategyContext.getCalculateOrgChildren()) ? " min(org.markaz_title)    orgTitle,\n min(org.hozeh_title)        childOrgTitle,\n" : " min(org.markaz_title)      orgTitle,\n";
            case HOZE ->Boolean.TRUE.equals(strategyContext.getCalculateOrgChildren()) ? " min(org.hozeh_title)     orgTitle,\n min(org.paygah_title)       childOrgTitle,\n" : " min(org.hozeh_title)       orgTitle,\n";
            default -> " min(org.paygah_title)      orgTitle,\n";
        };
    }

    protected String generateWhereStatement(StrategyContext strategyContext, String columnName, Map<String, Object> placeholdersWithValues) {
        var listOfFilters = new ArrayList<String>();
        setFilter(1, " org.org_is_enable= :isEnable", listOfFilters, "isEnable", placeholdersWithValues);
        setFilter(null, generateColumnNameIsNotNull(strategyContext), listOfFilters, null, null);
        if (!Objects.equals(strategyContext.getLayerNumOfSelectedOrgIds(), EN_OrgTypeLayerNum.SETAD.getId())) {
            setFilter(strategyContext.getOrgIds(), columnName + " IN (:orgIds) ", listOfFilters, "orgIds", placeholdersWithValues);
        }
        return listOfFilters.stream().filter(condition -> !condition.isEmpty()).collect(Collectors.joining("\n and "));
    }

    private String generateColumnNameIsNotNull(StrategyContext strategyContext) {
        String result;
        switch (EN_OrgTypeLayerNum.getById(strategyContext.getLayerNumOfSelectedOrgIds())) {
            case SETAD -> result = Boolean.TRUE.equals(strategyContext.getCalculateOrgChildren()) ? " org.nahiyeh_id " : " org.ostan_id ";
            case OSTAN-> result = Boolean.TRUE.equals(strategyContext.getCalculateOrgChildren()) ? " org.nahiyeh_id " : "";
            case NAHIYEH, MARKAZ-> result = Boolean.TRUE.equals(strategyContext.getCalculateOrgChildren()) ? " org.hozeh_id " : "";
            case HOZE-> result = Boolean.TRUE.equals(strategyContext.getCalculateOrgChildren()) ? " org.paygah_id " : "";
            default -> result = "";
        }
        return !result.isEmpty() ? result + " is not null " : "";
    }

    private <T> void setFilter(T value, String condition, List<String> filters, String placeholder, Map<String, Object> placeholdersWithValues) {
        if (value == null) {
            filters.add(condition);
        } else {
            if (value instanceof List<?> list && list.isEmpty()) {
                throw new CustomParameterizeException(ErrorConstantsCore.ERR_INVALID_ID);
            }

            if (value instanceof LocalDate localDate) {
                placeholdersWithValues.put(placeholder, localDate.toString());
                filters.add(condition);
            } else {
                placeholdersWithValues.put(placeholder, value);
                filters.add(condition);
            }
        }
    }
}
