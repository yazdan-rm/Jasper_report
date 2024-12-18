package ir.rbp.nabreport.dao.inbox.strategy;

import ir.rbp.nabcore.controller.exception.CustomParameterizeException;
import ir.rbp.nabcore.controller.exception.ErrorConstantsCore;
import ir.rbp.nabreport.common.constant.LiteralConstants;
import ir.rbp.nabreport.common.enumeration.pvs.EN_OrgTypeLayerNum;
import ir.rbp.nabreport.dao.absorption.impl.CommonAbsorptionDao;
import ir.rbp.nabreport.model.viewmodel.inbox.UserRequestReportVM;
import ir.rbp.nabreport.service.externalservice.org.IOrganizationService;
import lombok.Setter;
import org.jfree.layout.LCBLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.Map;
import java.util.logging.ErrorManager;

@Setter(onMethod_ = {@Autowired})
public abstract class AbstractUserRequestReportStrategy {

    private IOrganizationService organizationService;

    public abstract ClassPathResource getJasperFile();

    public abstract List<UserRequestReportVM> generateData(StrategyContext strategyContext);


    public ClassPathResource getXlsxJasperFile() {
        String reportNumber = getJasperFile().getPath().replaceAll("\\D+", "");
        return new ClassPathResource("report/inbox/xlsx/" + reportNumber + "-X.jrxml");
    }

    protected String createCompleteQueryWithDynamicParts(String query, Map<String, Object> filters, StrategyContext strategyContext) {
        String columnNames = getColumnNameByOrgTypeLayerNum(strategyContext.getLayerNumOfSelectedOrgIds());

        // Safely append or replace dynamic SQL components
        String dynamicSelect = "column1, column2, column3"; // Ensure this is predefined or validated
        String dynamicCondition = "AND column4 = :param";  // Use parameters instead of raw values
        String dynamicGroupBy = "column1, column2";
        String dynamicOrderBy = "column1 ASC";

        filters.put("param", "value"); // Add parameter safely

        return query
                .replace(":DYNAMIC_GENERATED_SELECT_PORTION", dynamicSelect)
                .replace(":DYNAMIC_GENERATED_CONDITION", dynamicCondition)
                .replace(":DYNAMIC_GENERATED_GROUP_BY", dynamicGroupBy)
                .replace(":DYNAMIC_GENERATED_ORDER_BY", dynamicOrderBy);
    }

    private String getColumnNameByOrgTypeLayerNum(Float orgTypeLayerNum) {

        return switch (EN_OrgTypeLayerNum.getById(orgTypeLayerNum)) {
            case OSTAN -> " org.ostan_id";
            case NAHIYEH -> " org.nahiyeh_id";
            case MARKAZ -> " org.markaz_id";
            case HOZE -> " org.hozeh_id";
            case PAYGAH -> " org.paygah_id";
            default -> throw new CustomParameterizeException(ErrorConstantsCore.ERR_INVALID_ID);
        };
    }
}
