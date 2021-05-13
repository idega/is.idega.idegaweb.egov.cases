package is.idega.idegaweb.egov.cases.data.bean;

import java.io.Serializable;

import javax.persistence.Cacheable;
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

@Entity
@Table(name = TimeRegistrationTask.TABLE_NAME)
@Cacheable
@NamedQueries({
	@NamedQuery(name = TimeRegistrationTask.QUERY_GET_BY_ID, query = "from TimeRegistrationTask trt where trt.id = :" + TimeRegistrationTask.PARAM_ID),
	@NamedQuery(name = TimeRegistrationTask.QUERY_GET_ALL, query = "from TimeRegistrationTask trt order by trt.ordering"),
	@NamedQuery(name = TimeRegistrationTask.QUERY_GET_ALL_TOP_LEVEL, query = "from TimeRegistrationTask trt where trt.parentTask is null order by trt.ordering"),
	@NamedQuery(name = TimeRegistrationTask.QUERY_GET_BY_PARENT_ID, query = "from TimeRegistrationTask trt where trt.parentTask.id = :" + TimeRegistrationTask.PARAM_PARENT_ID + " order by trt.ordering"),
	@NamedQuery(name = TimeRegistrationTask.QUERY_GET_BY_NAME, query = "from TimeRegistrationTask trt where trt.name = :" + TimeRegistrationTask.PARAM_NAME)
})
public class TimeRegistrationTask implements Serializable {

	private static final long serialVersionUID = 2371164594127200344L;

	static final String TABLE_NAME = "time_registration_task";

	public static final String	COLUMN_ID = TABLE_NAME + "_id",
								COLUMN_NAME = "name",
								COLUMN_DESCRIPTION = "description",
								COLUMN_PARENT_TASK = "parent_task",
								COLUMN_ORDER = "ordering",

								QUERY_GET_BY_ID = "timeRegistrationTask.getById",
								QUERY_GET_ALL = "timeRegistrationTask.getAll",
								QUERY_GET_ALL_TOP_LEVEL = "timeRegistrationTask.getAllTopLevel",
								QUERY_GET_BY_PARENT_ID = "timeRegistrationTask.getByParentId",
								QUERY_GET_BY_NAME = "timeRegistrationTask.getByName",

								PARAM_ID = "id",
								PARAM_PARENT_ID = "parentId",
								PARAM_NAME = "name";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = COLUMN_ID)
	private Long id;

	@Column(name = COLUMN_NAME, nullable = false)
	private String name;

	@Column(name = COLUMN_DESCRIPTION)
	private String description;

	@Column(name = COLUMN_ORDER)
	private Integer ordering;

    @ManyToOne
	@JoinColumn(name = COLUMN_PARENT_TASK, referencedColumnName = COLUMN_ID)
    private TimeRegistrationTask parentTask;

	public TimeRegistrationTask() {
		super();
	}

	public TimeRegistrationTask(String name, String description, Integer ordering) {
		this();

		this.name = name;
		this.description = description;
		this.ordering = ordering;
	}

	public TimeRegistrationTask(String name, String description, Integer ordering, TimeRegistrationTask parentTask) {
		this();

		this.name = name;
		this.description = description;
		this.ordering = ordering;
		this.parentTask = parentTask;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getOrdering() {
		return ordering;
	}

	public void setOrdering(Integer ordering) {
		this.ordering = ordering;
	}

	public TimeRegistrationTask getParentTask() {
		return parentTask;
	}

	public void setParentTask(TimeRegistrationTask parentTask) {
		this.parentTask = parentTask;
	}

	@Override
	public String toString() {
		return "ID: " + getId() + ", name: " + getName();
	}

}