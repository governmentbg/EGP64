package com.ib.docu.beans;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;

import com.ib.docu.archimed.Task;
import com.ib.docu.search.TaskSearch;
import com.ib.docu.system.UserData;
import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.indexui.system.IndexUIbean;

@Named(value = "testResolveTicket")
@ViewScoped
public class TestResolveTicket extends IndexUIbean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String Test_Resolve_Ticket = "testResolve";
	
	private LazyDataModelSQL2Array resTickList;
	private Task task = new Task();
	
	@PostConstruct
	public void initData() {
		
		TaskSearch tmpTs = new TaskSearch(getUserData(UserData.class).getRegistratura()); 
		tmpTs.setDocId(Integer.valueOf(12544));	
		tmpTs.buildQueryTasksInDoc();
		setResTickList(new LazyDataModelSQL2Array(tmpTs, "a1 asc"));
	}
	
	//private boolean checkData() {
		
	//}
	
	public LazyDataModelSQL2Array getResTickList() {
		return resTickList;
	}
	public void setResTickList(LazyDataModelSQL2Array resTickList) {
		this.resTickList = resTickList;
	}

	public static String getTestResolveTicket() {
		return Test_Resolve_Ticket;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}
	
	
	
	
}
