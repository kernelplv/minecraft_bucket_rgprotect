/**
 *
 * @author kernel plevitsky
 */
package rgprotect;

import com.sk89q.worldguard.LocalPlayer;
import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import java.sql.SQLException;
import java.util.logging.Level;
import org.bukkit.block.Block;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import static java.time.Clock.system;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import org.bukkit.event.block.BlockBreakEvent;


@SuppressWarnings("empty-statement")
public class rgprotect extends JavaPlugin implements Listener {
    public static final Logger _log = Logger.getLogger("RGPROTECT");

    
     private Player p;
     private LocalPlayer lp;
     WorldGuardPlugin worldGuard = WGBukkit.getPlugin();
     RegionManager ReM;
     ApplicableRegionSet set;

    @Override
      public void onEnable() {
          _log.info("[RGPROTECT] is loaded");
          _log.log(Level.INFO, "[RGPROTECT] WG database_path: {0}. Check it!", db.dbpath);
          _log.log(Level.INFO, "[RGPROTECT] WG world connected: {0}. Check it!", getServer().getWorlds().get(0).getName());
          ReM = worldGuard.getRegionManager(getServer().getWorlds().get(0));
          Bukkit.getPluginManager().registerEvents(this, this);
          
        try {
            db.Conn();
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(rgprotect.class.getName()).log(Level.SEVERE, "Cant connect to sqlite WG!", ex);
        }
        try {
            db.CreateDB();
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(rgprotect.class.getName()).log(Level.SEVERE, "Cant create new table in WG database!", ex);
        }
        try {
            db.ClearOldRecords("3"); //how old record(in days) must be deleted
        } catch (SQLException ex) {
            _log.info("[RGPROTECT] all records up to date!");
        }
        
      }
      
    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event){
        Block whb = event.getBlock();
        
        set= ReM.getApplicableRegions(whb.getLocation());
        Iterator<ProtectedRegion> iterr = set.iterator();
        Iterator<UUID> iterp;
           
        p = event.getPlayer();
        lp=worldGuard.wrapPlayer(p);
            
        if (!set.isOwnerOfAll(lp)){
            p.sendMessage(ChatColor.RED + "У тебя нет прав ломать этот блок! Действия зарегистрированы!");
            //    
                while (iterr.hasNext()){
            //        
                    iterp = iterr.next().getOwners().getUniqueIds().iterator();
                            
                    while (iterp.hasNext()){
            //            
                        try {
                            db.WriteDB(lp.getName(),iterp.next().toString());
                        } 
                        catch (SQLException ex) {
                            Logger.getLogger(rgprotect.class.getName()).log(Level.SEVERE, "Cant add new record to table in WG databese! Maby nickname is exists:)", ex);
                        }   
                    }
                }
            }
    }
      
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        ArrayList<String> damagers = new ArrayList<>();
        ArrayList<String> damagerst = new ArrayList<>();
        Iterator<String> itr = null;
        Iterator<String> itrt = null;
        String cmd="";
        String cmd2="";
        
        if (args.length>0) cmd=args[0];
        if (args.length>1) cmd2=args[1];
        
        if (command.getName().equalsIgnoreCase("rg"))
        {
        
                if ("".equals(cmd)){sender.sendMessage(ChatColor.GOLD + "RGPROTECT: /rg [ protect | whowashere nickname], ");}
                else{
                        if("protect".equals(cmd)){
                                        sender.sendMessage(ChatColor.GOLD + "RGPROTECT v.0.1");
                                        return true;}

                        else if(("whowashere".equals(cmd))&&("".equals(cmd2))){
                                        
                                        try {
                                            damagers.addAll(db.ReadDB(getServer().getPlayer(sender.getName()).getUniqueId().toString()));
                                            damagerst.addAll(db.ReadDBT(getServer().getPlayer(sender.getName()).getUniqueId().toString()));
                                            if (damagers.isEmpty()){sender.sendMessage(ChatColor.GREEN + "| Ваш регион не был атакован!"); return true;}
                                            else if (damagerst.isEmpty()){sender.sendMessage(ChatColor.GREEN + "| Ваш регион не был атакован!"); return true;}
                                            else
                                            {
                                                    sender.sendMessage(ChatColor.RED + "| Ваш регион был атакован:");
                                                    itr = damagers.iterator();
                                                    itrt = damagerst.iterator();
                                            
                                             while(itr.hasNext()&& itrt.hasNext())
                                                {
                                                    sender.sendMessage(ChatColor.RED+"| "+itr.next()+" | "+itrt.next());
                                                }
                                                sender.sendMessage(ChatColor.RED+"| Конец списка");
                                            }
                                        } catch (ClassNotFoundException | SQLException ex) {
                                          Logger.getLogger(rgprotect.class.getName()).log(Level.SEVERE, "Cant read record in WG database!", ex);
                                        }

                                                return true;
                                        }
                        else if(("whowashere".equals(cmd))&&(!"".equals(cmd2))) {
                                        try{ 
                                            cmd2=getServer().getOfflinePlayer(cmd2).getUniqueId().toString();
                                            System.out.print("!!!!!!!!!!!!!!!!!-"+cmd2);
                                            damagers.addAll(db.ReadDB(cmd2));
                                            damagerst.addAll(db.ReadDBT(cmd2));
                                            if (damagers.isEmpty()){sender.sendMessage(ChatColor.GREEN + "| Регион "+cmd2+" не был атакован!"); return true;}
                                            else if (damagerst.isEmpty()){sender.sendMessage(ChatColor.GREEN + "| Регион "+cmd2+" не был атакован!"); return true;}
                                            else
                                            {
                                                    sender.sendMessage(ChatColor.RED + "| Регион "+cmd2+" был атакован:");
                                                    itr = damagers.iterator();
                                                    itrt = damagerst.iterator();
                                            
                                             while(itr.hasNext()&& itrt.hasNext())
                                             {
                                                    sender.sendMessage(ChatColor.RED+"| "+itr.next()+" | "+itrt.next());
                                                }
                                                sender.sendMessage(ChatColor.RED+"| Конец списка");
                                            }
                                        } catch (ClassNotFoundException | SQLException ex) {
                                          Logger.getLogger(rgprotect.class.getName()).log(Level.SEVERE, "Cant read record in WG database!", ex);
                                        }

                                                return true;
                         }
                }
        }
            
        return false;
    }
    @Override
    
      public void onDisable() {
          _log.info("[RGPROTECT] closing...");
        
          try {
            db.CloseDB();
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(rgprotect.class.getName()).log(Level.SEVERE, "Cant close WG database connection!", ex);
        }
          
          _log.info("[RGPROTECT] WG database disconnected!");
      }
}
