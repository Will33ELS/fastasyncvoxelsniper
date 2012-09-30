package com.thevoxelbox.voxelsniper.brush;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.thevoxelbox.voxelsniper.Message;
import com.thevoxelbox.voxelsniper.SnipeData;
import com.thevoxelbox.voxelsniper.Undo;

/**
 * http://www.voxelwiki.com/minecraft/Voxelsniper#The_Erosion_Brush
 * @author Piotr, (lift preset by Giltwist)
 */
public class ErodeBrush extends Brush {

    private class BlockWrapper {

        public boolean solid;
        public Block nativeBlock;
        public int id;
        public int dataId;

        public BlockWrapper(final Block bl) {
            this.nativeBlock = bl;
            this.dataId = bl.getTypeId();
            switch (bl.getType()) {
            case AIR:
                this.solid = false;
                break;

            case WATER:
                this.solid = false;
                break;

            case STATIONARY_WATER:
                this.solid = false;
                break;

            case STATIONARY_LAVA:
                this.solid = false;
                break;
            case LAVA:
                this.solid = false;
                break;

            default:
                this.solid = true;
            }
        }
    }

    private static int timesUsed = 0;

    private BlockWrapper[][][] snap;
    private BlockWrapper[][][] firstSnap;
    private int bsize;
    private int erodeFace;
    private int fillFace;
    private int brushSize;
    private int erodeRecursion = 1;
    private int fillRecursion = 1;
    private double trueCircle = 0.5;
    private boolean reverse = false;

    /**
     * 
     */
    public ErodeBrush() {
        this.setName("Erode");
    }

    private boolean erode(final int x, final int y, final int z) {
    	int _d = 0;

    	if (this.snap[x][y][z].solid) {
            if (!this.snap[x + 1][y][z].solid) {
                _d++;
            }
            if (!this.snap[x - 1][y][z].solid) {
                _d++;
            }
            if (!this.snap[x][y + 1][z].solid) {
                _d++;
            }
            if (!this.snap[x][y - 1][z].solid) {
                _d++;
            }
            if (!this.snap[x][y][z + 1].solid) {
                _d++;
            }
            if (!this.snap[x][y][z - 1].solid) {
                _d++;
            }
            return _d >= this.erodeFace;
        } else {
            return false;
        }
    }

    private void erosion(final SnipeData v) {
    	final Undo _undo = new Undo(this.getTargetBlock().getWorld().getName());
    	final int _bSizePlusOne = this.bsize + 1;
    	final double _bPow = Math.pow(this.bsize + this.trueCircle, 2);
    	final double _bPowTC = Math.pow(this.bsize + 0.5, 2);
    	int _temp;
    	
    	if (this.reverse) {    		
            _temp = this.erodeFace;
            this.erodeFace = this.fillFace;
            this.fillFace = _temp;
            
            _temp = this.erodeRecursion;
            this.erodeRecursion = this.fillRecursion;
            this.fillRecursion = _temp;
        }

        if (this.erodeFace >= 0 && this.erodeFace <= 6) {
            for (int _er = 0; _er < this.erodeRecursion; _er++) {
                this.getMatrix();
                
                for (int _z = 1; _z < this.snap.length - 1; _z++) {

                    final double _zpow = Math.pow(_z - _bSizePlusOne, 2);
                    for (int _x = 1; _x < this.snap.length - 1; _x++) {

                        final double _xpow = Math.pow(_x - _bSizePlusOne, 2);
                        for (int _y = 1; _y < this.snap.length - 1; _y++) {

                            if (((_xpow + Math.pow(_y - _bSizePlusOne, 2) + _zpow) <= _bPow)) {
                                if (this.erode(_x, _y, _z)) {
                                    this.snap[_x][_y][_z].nativeBlock.setType(Material.AIR);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (this.fillFace >= 0 && this.fillFace <= 6) {
            for (int _fr = 0; _fr < this.fillRecursion; _fr++) {
                this.getMatrix();

                for (int _z = 1; _z < this.snap.length - 1; _z++) {

                    final double _zpow = Math.pow(_z - _bSizePlusOne, 2);
                    for (int _x = 1; _x < this.snap.length - 1; _x++) {

                        final double _xpow = Math.pow(_x - _bSizePlusOne, 2);
                        for (int _y = 1; _y < this.snap.length - 1; _y++) {

                            if (((_xpow + Math.pow(_y - _bSizePlusOne, 2) + _zpow) <= _bPowTC)) {
                                if (this.fill(_x, _y, _z)) {
                                    this.snap[_x][_y][_z].nativeBlock.setTypeId(this.snap[_x][_y][_z].id);
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int _x = 0; _x < this.firstSnap.length; _x++) {
            for (int _y = 0; _y < this.firstSnap.length; _y++) {
                for (int _z = 0; _z < this.firstSnap.length; _z++) {
                	
                    final BlockWrapper _block = this.firstSnap[_x][_y][_z];
                    if (_block.dataId != _block.nativeBlock.getTypeId()) {
                        _undo.put(_block.nativeBlock);
                    }
                    
                }
            }
        }

        v.storeUndo(_undo);
        
        if (this.reverse) { // if you dont put it back where it was, powder flips back and forth from fill to erode each time
            _temp = this.erodeFace;
            this.erodeFace = this.fillFace;
            this.fillFace = _temp;
            
            _temp = this.erodeRecursion;
            this.erodeRecursion = this.fillRecursion;
            this.fillRecursion = _temp;
        }
    }

    private boolean fill(final int x, final int y, final int z) {
    	int _d = 0;

    	if (this.snap[x][y][z].solid) {
            return false;
        } else {
            if (this.snap[x + 1][y][z].solid) {
                this.snap[x][y][z].id = this.snap[x + 1][y][z].nativeBlock.getTypeId();
                _d++;
            }
            if (this.snap[x - 1][y][z].solid) {
                this.snap[x][y][z].id = this.snap[x - 1][y][z].nativeBlock.getTypeId();
                _d++;
            }
            if (this.snap[x][y + 1][z].solid) {
                this.snap[x][y][z].id = this.snap[x][y + 1][z].nativeBlock.getTypeId();
                _d++;
            }
            if (this.snap[x][y - 1][z].solid) {
                this.snap[x][y][z].id = this.snap[x][y - 1][z].nativeBlock.getTypeId();
                _d++;
            }
            if (this.snap[x][y][z + 1].solid) {
                this.snap[x][y][z].id = this.snap[x][y][z + 1].nativeBlock.getTypeId();
                _d++;
            }
            if (this.snap[x][y][z - 1].solid) {
                this.snap[x][y][z].id = this.snap[x][y][z - 1].nativeBlock.getTypeId();
                _d++;
            }
            if (_d >= this.fillFace) {
                return true;
            } else {
                return false;
            }
        }
    }
    
    private void getMatrix() {
    	final int _v = (this.bsize + 1);
        this.brushSize = ((this.bsize + 1) * 2) + 1;

        if (this.snap.length == 0) {
            this.snap = new BlockWrapper[this.brushSize][this.brushSize][this.brushSize];

            int _sx = this.getBlockPositionX() - (this.bsize + 1);
            int _sy = this.getBlockPositionY() - (this.bsize + 1);
            int _sz = this.getBlockPositionZ() - (this.bsize + 1);
            for (int _x = 0; _x < this.snap.length; _x++) {
                _sz = this.getBlockPositionZ() - _v;
                for (int _z = 0; _z < this.snap.length; _z++) {
                    _sy = this.getBlockPositionY() - _v;
                    for (int _y = 0; _y < this.snap.length; _y++) {
                        this.snap[_x][_y][_z] = new BlockWrapper(this.clampY(_sx, _sy, _sz));
                        _sy++;
                    }
                    _sz++;
                }
                _sx++;
            }
            this.firstSnap = this.snap.clone();
        } else {
            this.snap = new BlockWrapper[this.brushSize][this.brushSize][this.brushSize];

            int _sx = this.getBlockPositionX() - (this.bsize + 1);
            int _sy = this.getBlockPositionY() - (this.bsize + 1);
            int _sz = this.getBlockPositionZ() - (this.bsize + 1);
            for (int _x = 0; _x < this.snap.length; _x++) {
                _sz = this.getBlockPositionZ() - _v;
                for (int _z = 0; _z < this.snap.length; _z++) {
                    _sy = this.getBlockPositionY() - _v;
                    for (int _y = 0; _y < this.snap.length; _y++) {
                        this.snap[_x][_y][_z] = new BlockWrapper(this.clampY(_sx, _sy, _sz));
                        _sy++;
                    }
                    _sz++;
                }
                _sx++;
            }
        }
    }

    @Override
    protected final void arrow(final SnipeData v) {
        this.bsize = v.getBrushSize();

        this.snap = new BlockWrapper[0][0][0];
        this.reverse = false;

        this.erosion(v);
    }

    @Override
    protected final void powder(final SnipeData v) {
        this.bsize = v.getBrushSize();

        this.snap = new BlockWrapper[0][0][0];
        this.reverse = true;

        this.erosion(v);
    }
    
    @Override
    public final void info(final Message vm) {
    	vm.brushName(this.getName());
    	vm.size();
    	vm.custom(ChatColor.RED + "Litesnipers: This is a slow brush. DO NOT SPAM it too much or hold down the mouse.");
    	vm.custom(ChatColor.AQUA + "Erosion minimum exposed faces set to " + this.erodeFace);
    	vm.custom(ChatColor.BLUE + "Fill minumum touching faces set to " + this.fillFace);
    	vm.custom(ChatColor.DARK_BLUE + "Erosion recursion amount set to " + this.erodeRecursion);
    	vm.custom(ChatColor.DARK_GREEN + "Fill recursion amount set to " + this.fillRecursion);
    }
    
    @Override
    public final void parameters(final String[] par, final com.thevoxelbox.voxelsniper.SnipeData v) {    	
    	for (int _i = 1; _i < par.length; _i++) {
    		final String _param = par[_i];
    		
    		try {
    			if (_param.equalsIgnoreCase("info")) {
    	    		v.sendMessage(ChatColor.GOLD + "Erode brush parameters");
    	    		v.sendMessage(ChatColor.RED + "NOT for litesnipers:");
    	    		v.sendMessage(ChatColor.GREEN + "b[number] (ex:   b23) Sets your sniper brush size.");
    	    		v.sendMessage(ChatColor.AQUA + "e[number] (ex:  e3) Sets the number of minimum exposed faces to erode a block.");
    	    		v.sendMessage(ChatColor.BLUE + "f[number] (ex:  f5) Sets the number of minumum faces containing a block to place a block.");
    	    		v.sendMessage(ChatColor.DARK_BLUE + "re[number] (ex:  re3) Sets the number of recursions the brush will perform erosion.");
    	    		v.sendMessage(ChatColor.DARK_GREEN + "rf[number] (ex:  rf5) Sets the number of recursions the brush will perform filling.");
    	    		v.sendMessage(ChatColor.AQUA + "/b d false -- will turn off true circle algorithm /b b true will switch back. (true is default for this brush.)");
    	    		v.sendMessage(ChatColor.GOLD + "For user-friendly pre-sets, type /b e info2.");
    	    		return;
    	    	}
    	    	if (_param.equalsIgnoreCase("info2")) {
    	    		v.sendMessage(ChatColor.GOLD
    	    				+ "User-friendly Preset Options.  These are for the arrow.  Powder will do reverse for the first two (for fast switching):");
    	    		v.sendMessage(ChatColor.BLUE + "OK for litesnipers:");
    	    		v.sendMessage(ChatColor.GREEN + "/b e melt -- for melting away protruding corners and edges.");
    	    		v.sendMessage(ChatColor.AQUA + "/b e fill -- for building up inside corners");
    	    		v.sendMessage(ChatColor.AQUA
    	    				+ "/b e smooth -- For the most part, does not change total number of blocks, but smooths the shape nicely. Use as a finishing touch for the most part, before overlaying grass and trees, etc.");
    	    		v.sendMessage(ChatColor.BLUE + "/b e lift-- More or less raises each block in the brush area blockPositionY one");
    	    		return;
    	    	}
    			if (_param.startsWith("melt")) {
    				this.fillRecursion = 1;
    				this.erodeRecursion = 1;
    				this.fillFace = 5;
    				this.erodeFace = 2;
    				v.owner().setBrushSize(10);
    				v.sendMessage(ChatColor.AQUA + "Melt mode. (/b e e2 f5 re1 rf1 b10)");
    				continue;
    			} else if (_param.startsWith("fill")) {
    				this.fillRecursion = 1;
    				this.erodeRecursion = 1;
    				this.fillFace = 2;
    				this.erodeFace = 5;
    				v.owner().setBrushSize(8);
    				v.sendMessage(ChatColor.AQUA + "Fill mode. (/b e e5 f2 re1 rf1 b8)");
    				continue;
    			} else if (_param.startsWith("smooth")) {
    				this.fillRecursion = 1;
    				this.erodeRecursion = 1;
    				this.fillFace = 3;
    				this.erodeFace = 3;
    				v.owner().setBrushSize(16);
    				v.sendMessage(ChatColor.AQUA + "Smooth mode. (/b e e3 f3 re1 rf1 b16)");
    				continue;
    			} else if (_param.startsWith("lift")) {
    				this.fillRecursion = 1;
    				this.erodeRecursion = 0;
    				this.fillFace = 1;
    				this.erodeFace = 6;
    				v.owner().setBrushSize(10);
    				v.sendMessage(ChatColor.AQUA + "Lift mode. (/b e e6 f1 re0 rf1 b10)");
    				continue;
    			} else if (_param.startsWith("true")) {
    				this.trueCircle = 0.5;
    				v.sendMessage(ChatColor.AQUA + "True circle mode ON." + this.erodeRecursion);
    				continue;
    			} else if (_param.startsWith("false")) {
    				this.trueCircle = 0;
    				v.sendMessage(ChatColor.AQUA + "True circle mode OFF." + this.erodeRecursion);
    				continue;
    			} else if (_param.startsWith("rf")) {
    				this.fillRecursion = Integer.parseInt(_param.replace("rf", ""));
    				v.sendMessage(ChatColor.BLUE + "Fill recursion amount set to " + this.fillRecursion);
    				continue;
    			} else if (_param.startsWith("re")) {
    				this.erodeRecursion = Integer.parseInt(_param.replace("re", ""));
    				v.sendMessage(ChatColor.AQUA + "Erosion recursion amount set to " + this.erodeRecursion);
    				continue;    				
    			} else if (_param.startsWith("f")) {
    				this.fillFace = Integer.parseInt(_param.replace("f", ""));
    				v.sendMessage(ChatColor.BLUE + "Fill minumum touching faces set to " + this.fillFace);
    				continue;
    			} else if (_param.startsWith("b")) {
    				v.owner().setBrushSize(Integer.parseInt(_param.replace("b", "")));
    				continue;
    			} else if (_param.startsWith("e")) {
    				this.erodeFace = Integer.parseInt(_param.replace("e", ""));
    				v.sendMessage(ChatColor.AQUA + "Erosion minimum exposed faces set to " + this.erodeFace);
    				continue;
    			} else {
    				v.sendMessage(ChatColor.RED + "Invalid brush parameters! use the info parameter to display parameter info.");
    			}
    		} catch (final Exception _e) {
    			v.sendMessage(ChatColor.RED + "Invalid brush parameters! \"" + _param
    					+ "\" is not a valid statement. Please use the 'info' parameter to display parameter info.");
    		}
    	}
    }

    @Override
    public final int getTimesUsed() {
    	return ErodeBrush.timesUsed;
    }
    
    @Override
    public final void setTimesUsed(final int tUsed) {
    	ErodeBrush.timesUsed = tUsed;
    }
}
