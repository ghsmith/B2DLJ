package b2dlj.finder;

import b2dlj.data.PathologyCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class PathologyCaseFinder{

    private final static Logger LOGGER = LoggerFactory.getLogger(PathologyCaseFinder.class);

    Connection connClr;
    PreparedStatement pstmt;

    String salt;

    public PathologyCaseFinder(Connection connClr, String salt) throws SQLException {

        this.salt = salt;

        this.connClr = connClr;
        pstmt = connClr.prepareStatement("""
select
  lcdm.case_id,
  convert(varchar(32), hashbytes('MD5', ? + cast(lcdm.case_id as varchar)), 2) case_id_study,
  lci.case_num,
  lcdm.case_coll_dttm,
  (
    select
      string_agg(cast(orcc.results_comp_cmt as varchar(MAX)), char(10)) within group(order by orcc.line_comp, orcc.line_comment)
    from
      (select max(rog.order_id) max_order_id from lab_case_info lci join req_order_group rog on(lci.requisition_id = rog.requisition_id) where lci.requisition_id = lcdm.case_id) x
      join order_res_comp_cmt orcc on(max_order_id = orcc.order_id)
    where
      orcc.component_id = '185'
  ) addendum5_dx,
  (
    select
      string_agg(cast(orcc.results_comp_cmt as varchar(MAX)), char(10)) within group(order by orcc.line_comp, orcc.line_comment)
    from
      (select max(rog.order_id) max_order_id from lab_case_info lci join req_order_group rog on(lci.requisition_id = rog.requisition_id) where lci.requisition_id = lcdm.case_id) x
      join order_res_comp_cmt orcc on(max_order_id = orcc.order_id)
    where
      orcc.component_id = '184'
  ) addendum4_dx,
  (
    select
      string_agg(cast(orcc.results_comp_cmt as varchar(MAX)), char(10)) within group(order by orcc.line_comp, orcc.line_comment)
    from
      (select max(rog.order_id) max_order_id from lab_case_info lci join req_order_group rog on(lci.requisition_id = rog.requisition_id) where lci.requisition_id = lcdm.case_id) x
      join order_res_comp_cmt orcc on(max_order_id = orcc.order_id)
    where
      orcc.component_id = '183'
  ) addendum3_dx,
  (
    select
      string_agg(cast(orcc.results_comp_cmt as varchar(MAX)), char(10)) within group(order by orcc.line_comp, orcc.line_comment)
    from
      (select max(rog.order_id) max_order_id from lab_case_info lci join req_order_group rog on(lci.requisition_id = rog.requisition_id) where lci.requisition_id = lcdm.case_id) x
      join order_res_comp_cmt orcc on(max_order_id = orcc.order_id)
    where
      orcc.component_id = '182'
  ) addendum2_dx,
  (
    select
      string_agg(cast(orcc.results_comp_cmt as varchar(MAX)), char(10)) within group(order by orcc.line_comp, orcc.line_comment)
    from
      (select max(rog.order_id) max_order_id from lab_case_info lci join req_order_group rog on(lci.requisition_id = rog.requisition_id) where lci.requisition_id = lcdm.case_id) x
      join order_res_comp_cmt orcc on(max_order_id = orcc.order_id)
    where
      orcc.component_id = '181'
  ) addendum1_dx,
  (
    select
      string_agg(cast(orcc.results_comp_cmt as varchar(MAX)), char(10)) within group(order by orcc.line_comp, orcc.line_comment)
    from
      (select max(rog.order_id) max_order_id from lab_case_info lci join req_order_group rog on(lci.requisition_id = rog.requisition_id) where lci.requisition_id = lcdm.case_id) x
      join order_res_comp_cmt orcc on(max_order_id = orcc.order_id)
    where
      orcc.component_id = '180'
  ) final_dx,
  (
    select
      string_agg(cast(orcc.results_comp_cmt as varchar(MAX)), char(10)) within group(order by orcc.line_comp, orcc.line_comment)
    from
      (select max(rog.order_id) max_order_id from lab_case_info lci join req_order_group rog on(lci.requisition_id = rog.requisition_id) where lci.requisition_id = lcdm.case_id) x
      join order_res_comp_cmt orcc on(max_order_id = orcc.order_id)
    where
      orcc.component_id = '244'
  ) dx_comment,
  (
    select
      string_agg(cast(orcc.results_comp_cmt as varchar(MAX)), char(10)) within group(order by orcc.line_comp, orcc.line_comment)
    from
      (select max(rog.order_id) max_order_id from lab_case_info lci join req_order_group rog on(lci.requisition_id = rog.requisition_id) where lci.requisition_id = lcdm.case_id) x
      join order_res_comp_cmt orcc on(max_order_id = orcc.order_id)
    where
      orcc.component_id = '179'
  ) special_stains,
  (
    select
      string_agg(cast(orcc.results_comp_cmt as varchar(MAX)), char(10)) within group(order by orcc.line_comp, orcc.line_comment)
    from
      (select max(rog.order_id) max_order_id from lab_case_info lci join req_order_group rog on(lci.requisition_id = rog.requisition_id) where lci.requisition_id = lcdm.case_id) x
      join order_res_comp_cmt orcc on(max_order_id = orcc.order_id)
    where
      orcc.component_id = '1230000057'
  ) gross_descr,
  (
    select
      string_agg(cast(orcc.results_comp_cmt as varchar(MAX)), char(10)) within group(order by orcc.line_comp, orcc.line_comment)
    from
      (select max(rog.order_id) max_order_id from lab_case_info lci join req_order_group rog on(lci.requisition_id = rog.requisition_id) where lci.requisition_id = lcdm.case_id) x
      join order_res_comp_cmt orcc on(max_order_id = orcc.order_id)
    where
      orcc.component_id = '140'
  ) micro_descr,
  (
    select
      string_agg(cast(orcc.results_comp_cmt as varchar(MAX)), char(10)) within group(order by orcc.line_comp, orcc.line_comment)
    from
      (select max(rog.order_id) max_order_id from lab_case_info lci join req_order_group rog on(lci.requisition_id = rog.requisition_id) where lci.requisition_id = lcdm.case_id) x
      join order_res_comp_cmt orcc on(max_order_id = orcc.order_id)
    where
      orcc.component_id = '135'
  ) clinical_hx
from
  patient p
  join lab_case_db_main lcdm on(p.pat_id = lcdm.case_pat_id)
  join lab_case_info lci on(lcdm.case_id = lci.requisition_id)
where
  lci.case_num = ?
        """);

    }

    public PathologyCase getByAccessionNumber(String accessionNumber) throws SQLException {
        LOGGER.info(String.format("getByAccessionNumber: %s", accessionNumber));
        PathologyCase pathologyCase = null;
        pstmt.clearParameters();
        pstmt.setString(1, salt);
        pstmt.setString(2, accessionNumber);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()) {
            pathologyCase = new PathologyCase();
            pathologyCase.caseId = rs.getLong("case_id");
            pathologyCase.caseIdStudy = rs.getString("case_id_study");
            pathologyCase.accessionNumber = rs.getString("case_num");
            pathologyCase.collectionDate = new Date(rs.getDate("case_coll_dttm").getTime());
            pathologyCase.addendum5Dx = rs.getString("addendum5_dx");
            pathologyCase.addendum4Dx = rs.getString("addendum4_dx");
            pathologyCase.addendum3Dx = rs.getString("addendum3_dx");
            pathologyCase.addendum2Dx = rs.getString("addendum2_dx");
            pathologyCase.addendum1Dx = rs.getString("addendum1_dx");
            pathologyCase.finalDx = rs.getString("final_dx");
            pathologyCase.dxComment = rs.getString("dx_comment");
            pathologyCase.specialStains = rs.getString("special_stains");
            pathologyCase.grossDescr = rs.getString("gross_descr");
            pathologyCase.microDescr = rs.getString("micro_descr");
            pathologyCase.clinicalHx = rs.getString("clinical_hx");
        }
        return pathologyCase;
    }

}