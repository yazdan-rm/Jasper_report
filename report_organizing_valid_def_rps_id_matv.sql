create view REPORT_ORGANIZING_VALID_DEF_RPS_ID_MATV as
WITH Defs_IS_REQUIRED_OR_IS_HEAD_POST_OK AS (
      select deff.id, deff.fk_role_playing_set_id, deff.is_deleted, deff.FK_ORG_RPS_TYPE_ID, deff.FK_ORG_ID
      from  org$defined_role_playing_set deff where deff.id not in
      (
           select def_rps.id  from org$membership_organizing mem_org
           inner join org$defined_role_playing_set def_rps on mem_org.fk_def_rps_id = def_rps.id
           inner join org.org_chart_branch_post_n_matv OCBP ON OCBP.branch_post_id = mem_org.fk_branch_post_id
           where mem_org.fk_pv_archive_type_id = 891 and (OCBP.IS_REQUIRED = 1 or OCBP.IS_HEAD_POST = 1)
           and OCBP.is_deleted=0 and OCBP.CHART_IS_DELETED = 0 and OCBP.BRANCH_IS_DELETED=0
      )
  ),
  Defs_IS_MIN_MEMBER_OK as(
           select def_rps.id
                from org$membership_organizing mem_org
                inner join Defs_IS_REQUIRED_OR_IS_HEAD_POST_OK  def_rps on mem_org.fk_def_rps_id = def_rps.id
                inner join ORG.rps$role_playing_set rps on def_rps.fk_role_playing_set_id = rps.id
                where def_rps.is_deleted = 0 and mem_org.is_deleted = 0 and mem_org.FK_MEMBERSHIP_ID is not null and mem_org.fk_pv_archive_type_id=893
                and rps.is_deleted = 0 and rps.is_enabled = 1
                and mem_org.FK_CHART_BRANCH_ID in (
                    select CHART_BRANCH_ID from ORG.org_chart_branch_post_n_matv mtv where mtv.is_deleted=0 and mtv.CHART_IS_DELETED = 0 and mtv.BRANCH_IS_DELETED=0
                )
                group by def_rps.id
                having count(mem_org.id) >= min(rps.MIN_MEMBER_COUNT) or min(rps.MIN_MEMBER_COUNT) is null
          )
 select
    TBLODRPS.id def_rps_id,
    TBLODRPS.fk_org_id fk_org_id,
    rps.id rps_id,
    TBLODRPS.FK_ORG_RPS_TYPE_ID rps_type_id,
    nvl(rps.is_need_subjective_approve,0) is_need_approve,
    rps.day_deadline_subjective_approve rps_deadline
from org$defined_role_playing_set TBLODRPS
  inner join ORG.rps$role_playing_set rps on TBLODRPS.fk_role_playing_set_id = rps.id
where TBLODRPS.is_deleted = 0 and rps.is_deleted = 0 and rps.is_enabled = 1
and EXISTS (
            SELECT *
            FROM ORG$MEMBERSHIP_ORGANIZING OM
            WHERE OM.FK_DEF_RPS_ID=TBLODRPS.ID AND OM.FK_PV_ARCHIVE_TYPE_ID=893 AND OM.APPROVED_BY_SUBJECTIVE IS NULL
            and TBLODRPS.ID in (
                select def_rps.id
                from Defs_IS_MIN_MEMBER_OK def_rps
            )
         )
