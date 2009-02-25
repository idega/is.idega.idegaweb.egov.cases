function setSubCategories(data) {
	dwr.util.removeAllOptions("prm_sub_case_category_pk");
	dwr.util.addOptions("prm_sub_case_category_pk", data);
}

function changeSubCategories(inputID, country) {
	CasesBusiness.getAllSubCategories(dwr.util.getValue(inputID), country, setSubCategories);
}

function setUsers(data) {
	dwr.util.removeAllOptions("prm_user");
	dwr.util.addOptions("prm_user", data);
}

function changeUsers(inputID) {
	CasesBusiness.getUsers(dwr.util.getValue(inputID), setUsers);
}