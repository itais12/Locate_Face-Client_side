package model;

public class Worker {
	// {pinCode ,id, workerId,name}
	private boolean managerPermissions;
	private int id, workerId;
	private String name , pinCode, role, department, email;

	// constructor for JsonToObject
	public Worker() {
		setId(-1);
		setName("");
		setWorkerId(-1);
		setPinCode("");
		setRole("");
		setDepartment("");
		setEmail("");
		setManagerPermissions(false);
	}

	// constructor for register mode
	public Worker(int id, String name, String role, String department,boolean managerPermissions, String email){
		setId(id);
		setName(name);
		setWorkerId(-1);
		setPinCode("");
		setRole(role);
		setDepartment(department);
		setEmail(email);
		setManagerPermissions(managerPermissions);
	}

	// constructor for identify mode
	public Worker(String pinCode) {
		this();
		setPinCode(pinCode);
	}

	public int getWorkerId() {
		return workerId;
	}

	public void setWorkerId(int workerId) {
		this.workerId = workerId;
	}

	
	public String getPinCode() {
		return pinCode;
	}

	public void setPinCode(String pinCode) {
		this.pinCode = pinCode;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}


	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public boolean getManagerPermissions() {
		return managerPermissions;
	}

	public void setManagerPermissions(boolean managerPermissions) {
		this.managerPermissions = managerPermissions;
	}
	

}
