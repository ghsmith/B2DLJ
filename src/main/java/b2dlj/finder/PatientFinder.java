package b2dlj.finder;

import b2dlj.data.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class PatientFinder{

    private final static Logger LOGGER = LoggerFactory.getLogger(PatientFinder.class);

    Connection connClr;
    PreparedStatement pstmt;

    String salt;

    public PatientFinder(Connection connClr, String salt) throws SQLException {

        this.salt = salt;

        this.connClr = connClr;
        pstmt = connClr.prepareStatement("""
select
  p.pat_id,
  convert(varchar(32), hashbytes('MD5', ? + p.pat_id), 2) pat_id_study,
  p.pat_mrn_id,
  p.birth_date,
  (select abbr from zc_sex where rcpt_mem_sex_c = p.sex_c) sex,
  (select string_agg(zpr.abbr, ', ') from patient_race pr join zc_patient_race zpr on(pr.patient_race_c = zpr.patient_race_c) where pr.pat_id = p.pat_id) race
from
  patient p
  join lab_case_db_main lcdm on(p.pat_id = lcdm.case_pat_id)
  join lab_case_info lci on(lcdm.case_id = lci.requisition_id)
where
  lci.case_num = ?
        """);

    }

    public Patient getByAccessionNumber(String accessionNumber) throws SQLException{
        LOGGER.info(String.format("getByAccessionNumber: %s", accessionNumber));
        Patient patient = null;
        pstmt.clearParameters();
        pstmt.setString(1, salt);
        pstmt.setString(2, accessionNumber);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()) {
            patient = new Patient();
            patient.patId = rs.getString("pat_id");
            patient.patIdStudy = rs.getString("pat_id_study");
            patient.mrn =  rs.getString("pat_mrn_id");
            patient.dateOfBirth = new Date(rs.getDate("birth_date").getTime());
            patient.sex = rs.getString("sex");
            patient.race = rs.getString("race");
        }
        return patient;
    }

}