package dev.pizzawunsch.foodbugfix;

import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@Getter
public class FoodBugFix extends JavaPlugin {

    @Getter
    private static FoodBugFix instance;
    private List<Player> dropper;

    @Override
    public void onEnable() {
        instance = this;
        dropper = Lists.newArrayList();

        register("dev.pizzawunsch.foodbugfix.listener");
        PacketInterceptor.initialize();
    }

    private void register(String... listenerPackageNameList) {
        try {
            for (String listenerPackageName : listenerPackageNameList) {
                for (ClassPath.ClassInfo classInfo : ClassPath.from(getClassLoader())
                        .getTopLevelClasses(listenerPackageName)) {
                    Class<?> currentClass = Class.forName(classInfo.getName());
                    if (Listener.class.isAssignableFrom(currentClass))
                        Bukkit.getPluginManager().registerEvents((Listener) currentClass.newInstance(), this);
                }
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            getServer().getConsoleSender().sendMessage("[" + this.getName() + "] Could not load the FoodBugFix plugin due to a error occurred while registering the commands and listener.");
        }
    }

}
