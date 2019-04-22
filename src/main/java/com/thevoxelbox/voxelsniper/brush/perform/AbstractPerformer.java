/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.thevoxelbox.voxelsniper.brush.perform;

import com.thevoxelbox.voxelsniper.sniper.Undo;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

/**
 * @author Voxel
 */
public abstract class AbstractPerformer implements Performer {

	private String name;
	protected Undo undo;
	@Nullable
	protected World world;

	public AbstractPerformer(String name) {
		this.name = name;
	}

	@Override
	public void setUndo() {
		this.undo = new Undo();
	}

	@Override
	public Undo getUndo() {
		Undo temp = this.undo;
		this.undo = null;
		return temp;
	}

	@Override
	public boolean isUsingReplaceMaterial() {
		return false;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}
