package is.idega.idegaweb.egov.cases.data.bean;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.idega.user.data.bean.User;


@Entity
@Table(name = "time_spent_on_case")
@NamedQueries(
		{
			@NamedQuery(name="tsoc.getByCaseAndUser", query = "FROM TimeSpentOnCase tsoc WHERE tsoc.bpmCase =:"+TimeSpentOnCase.CaseIdProp+" and tsoc.user.userID =:"+TimeSpentOnCase.UserIdProp + " and tsoc.removed is null ORDER BY tsoc.start"),
			@NamedQuery(name="tsoc.getByCase", query = "FROM TimeSpentOnCase tsoc WHERE tsoc.bpmCase =:"+TimeSpentOnCase.CaseIdProp + " and tsoc.removed is null  ORDER BY tsoc.start"),
			@NamedQuery(name="tsoc.getByCaseUuid", query = "FROM TimeSpentOnCase tsoc WHERE tsoc.caseUuid =:"+TimeSpentOnCase.CaseUuidProp + " and tsoc.removed is null  ORDER BY tsoc.start"),
			@NamedQuery(name="tsoc.getByCaseRemoved", query = "FROM TimeSpentOnCase tsoc WHERE tsoc.bpmCase =:"+TimeSpentOnCase.CaseIdProp + " and tsoc.removed is not null  ORDER BY tsoc.start"),
			@NamedQuery(name="tsoc.getByCaseUuidRemoved", query = "FROM TimeSpentOnCase tsoc WHERE tsoc.caseUuid =:"+TimeSpentOnCase.CaseUuidProp + " and tsoc.removed is not null  ORDER BY tsoc.start"),
			@NamedQuery(name="tsoc.getAllActiveForUser", query = "FROM TimeSpentOnCase tsoc WHERE tsoc.user.userID =:"+TimeSpentOnCase.UserIdProp + " and tsoc.end is null and tsoc.removed is null ORDER BY tsoc.start")
		}
)
public class TimeSpentOnCase implements Serializable{

	private static final long serialVersionUID = 6008891131179251971L;

	public static final String getByCaseAndUser = "tsoc.getByCaseAndUser";
	public static final String getAllActiveForUser = "tsoc.getAllActiveForUser";
	public static final String getByCase = "tsoc.getByCase";
	public static final String getByCaseUuid = "tsoc.getByCaseUuid";
	public static final String getByCaseRemoved = "tsoc.getByCaseRemoved";
	public static final String getByCaseUuidRemoved = "tsoc.getByCaseUuidRemoved";
	public static final String UserIdProp = "userId";
	public static final String CaseIdProp = "caseId";
	public static final String CaseUuidProp = "caseUuid";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "ic_user_id", nullable = false)
	private User user;

	@Column(name ="case_id")
	private Long bpmCase;

	@Column(name ="case_uuid")
	private String caseUuid;

	@Column(name = "start_time")
	private Timestamp start;

	@Column(name = "end_time")
	private Timestamp end;

	@Column(name ="duration")
	private Long duration;

	@Column(name ="comment")
	private String comment;

	@Column(name = "date_removed")
	private Timestamp removed;

	@ManyToOne
	@JoinColumn(name = "removedBy", referencedColumnName="ic_user_id")
	private User removedBy;

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Long getBpmCase() {
		return bpmCase;
	}

	public void setBpmCase(Long bpmCase) {
		this.bpmCase = bpmCase;
	}

	public Timestamp getStart() {
		return start;
	}

	public void setStart(Timestamp start) {
		this.start = start;
	}

	public Timestamp getEnd() {
		return end;
	}

	public void setEnd(Timestamp end) {
		this.end = end;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public String getCaseUuid() {
		return caseUuid;
	}

	public void setCaseUuid(String caseUuid) {
		this.caseUuid = caseUuid;
	}

	public Timestamp getRemoved() {
		return removed;
	}

	public void setRemoved(Timestamp removed) {
		this.removed = removed;
	}

	public User getRemovedBy() {
		return removedBy;
	}

	public void setRemovedBy(User removedBy) {
		this.removedBy = removedBy;
	}
}
