package is.idega.idegaweb.egov.cases.data.bean;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.idega.user.data.bean.User;

import is.idega.idegaweb.egov.cases.data.GeneralCaseBMPBean;

@Entity
@Table(name = GeneralCase.ENTITY_NAME)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class GeneralCase implements Serializable {

	private static final long serialVersionUID = -5337237367929008410L;

	public static final String	ENTITY_NAME = GeneralCaseBMPBean.ENTITY_NAME,
								COLUMN_CASE_ID = ENTITY_NAME + "_ID";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = COLUMN_CASE_ID)
	private Integer id;

	@Column(name = GeneralCaseBMPBean.COLUMN_HANDLER)
	private User handler;

}