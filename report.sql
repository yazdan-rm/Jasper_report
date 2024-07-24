package ir.rbp.nab.model.domainmodel.membership;

import ir.rbp.nab.model.domainmodel.person.PSNPerson;
import ir.rbp.nab.model.domainmodel.setting.bi.BIMembershipTypeLevel;
import ir.rbp.nabcore.model.domainmodel.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.HashCodeExclude;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "MEM$MEMBERSHIP")//,uniqueConstraints = @UniqueConstraint(columnNames = {"START_DATE","FK_ORG_ID","FK_PV_MEMBERSHIP_TYPE_ID","FK_PERSON_ID"}))
@SequenceGenerator(initialValue = 100, name = "SEQ_GENERATOR", sequenceName = "MEM$MEMBERSHIP_SEQ", allocationSize = 1)
public class MEMMembership extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -4920436770452366817L;

    @NotNull
    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "IS_SHARED_FILE")
    private Boolean isSharedFile;

    @NotNull
    @Column(name = "IS_ARCHIVED", nullable = false)
    private Boolean isArchived;

    @NotNull
    @Column(name = "FK_ORG_ID", nullable = false)
    private Long orgId;

    @Column(name = "FK_ORG_CORTEX_ID")
    private Long orgCortexId;

    @Column(name = "FK_PV_MEMBERSHIP_TYPE_ID")
    private Long pvMembershipTypeId;

    @Column(name = "FK_PV_ARCHIVE_TYPE_ID")
    private Long pvArchiveTypeId;

    @Column(name = "FK_PV_DOWNGRADE_CATEGORY_ID")
    private Long pvDowngradeCategoryId;

    @Column(name = "IS_LAST_MEM")
    private Boolean isLastMembership;

    @Column(name = "FK_PV_DOWNGRADE_REASON_ID")
    private Long pvDowngradeReasonId;

    @Column(name ="DOWNGRADE_REASON_DESCRIPTION")
    private String downgradeReasonDescription;

    @Column(name = "FILE_NUMBER")
    private String fileNumber;
    /**
     * indicates that the membership is the first membership in the org or not,
     * in new persist and new subscription it must be set to true
     */
    @Column(name = "IS_FIRST_MEM")
    private Boolean isFirstMembership;

    @NotNull
    @Column(name = "FK_PV_MEM_STATUS_ID", nullable = false)
    private Long pvMemStatusId;

    @Column(name = "IS_MEM_STATUS_ID_CHANGED")
    private Boolean isMemStatusIdChanged;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_MEM_TYPE_LEVEL_ID")
    @HashCodeExclude
    private BIMembershipTypeLevel membershipTypeLevel;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST}, optional = false)
    @JoinColumn(name = "FK_PERSON_ID", nullable = false)
    private PSNPerson person;

}
