/*******************************************************************************
 * Copyright (c) 2012 sfleury.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     sfleury - initial API and implementation
 ******************************************************************************/
package org.gots.action;

import java.util.Calendar;
import java.util.Date;

import org.gots.bean.BaseAllotmentInterface;
import org.gots.seed.GrowingSeedInterface;

public abstract class AbstractActionGarden implements GardeningActionInterface {
	private int id;
	private String name;
	private String description;
	private int duration; // nb days before doing action
	private Date dateActionDone;
	private int state;
	private Date dateActionTodo;
	private int logid;
	private int growingSeedId;
	private Object data;

	@Override
	public Object getData() {
		return this.data;
	}

	@Override
	public void setData(Object data) {
		this.data = data;
	}

	public AbstractActionGarden() {
		// TODO Auto-generated constructor stub
	}

	public AbstractActionGarden(String name) {
		this.name = name;
	}

	@Override
	public int getGrowingSeedId() {
		return growingSeedId;
	}

	@Override
	public void setGrowingSeedId(int id) {
		growingSeedId = id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int getDuration() {
		return duration;
	}

	@Override
	public void setDuration(int duration) {
		this.duration = duration;
	}

	@Override
	public Date getDateActionDone() {
		return dateActionDone;
	}

	@Override
	public void setDateActionDone(Date dateActionDone) {
		this.dateActionDone = dateActionDone;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int execute(BaseAllotmentInterface allotment, GrowingSeedInterface seed) {
		setDateActionDone(Calendar.getInstance().getTime());
		return 1;
	}

	@Override
	public int getState() {
		return this.state;
	}

	@Override
	public void setState(int state) {
		this.state = state;

	}

	@Override
	public Date getDateActionTodo() {
		return this.dateActionTodo;
	}

	@Override
	public void setDateActionTodo(Date dateActionTodo) {
		this.dateActionTodo = dateActionTodo;

	}

	@Override
	public int getLogId() {
		return this.logid;
	}

	@Override
	public void setLogId(int id) {
		this.logid = id;

	}
}
