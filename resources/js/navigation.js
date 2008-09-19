function setSubCategories(data) {
	DWRUtil.removeAllOptions("prm_sub_case_category_pk");
	DWRUtil.addOptions("prm_sub_case_category_pk", data);
}

function changeSubCategories(inputID, country) {
	CasesDWRUtil.getSubCategories(DWRUtil.getValue(inputID), country, setSubCategories);
}

function setUsers(data) {
	DWRUtil.removeAllOptions("prm_user");
	DWRUtil.addOptions("prm_user", data);
}

function changeUsers(inputID) {
	CasesDWRUtil.getUsers(DWRUtil.getValue(inputID), setUsers);
}