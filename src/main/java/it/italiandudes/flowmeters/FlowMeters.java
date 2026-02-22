package it.italiandudes.flowmeters;

import it.italiandudes.flowmeters.proxy.CommonProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
@Mod(modid = FlowMeters.MODID, name = FlowMeters.NAME, version = FlowMeters.VERSION)
public class FlowMeters {

  // Mod Data
  public static final String MODID = "flowmeters";
  public static final String NAME = "Flow Meters";
  public static final String VERSION = "1.0.3B";

  // Logger
  public static final Logger LOGGER = LogManager.getLogger(MODID);

  // Network Wrapper
  public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

  @Instance
  public static FlowMeters INSTANCE;

  // Proxy
  @SidedProxy(
      serverSide = "it.italiandudes.flowmeters.proxy.CommonProxy",
      clientSide = "it.italiandudes.flowmeters.proxy.ClientProxy")
  public static CommonProxy PROXY;

  // Creative Tabs
  public static final CreativeTabs CREATIVE_TAB = new FlowMetersCreativeTab();

  // Event Handlers
  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    PROXY.preInit(event);
  }

  @EventHandler
  public void init(FMLInitializationEvent event) {
    PROXY.init(event);
  }

  @EventHandler
  public void init(FMLPostInitializationEvent event) {
    PROXY.postInit(event);
  }
}
