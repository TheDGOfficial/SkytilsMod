/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package gg.skytils.skytilsmod

import gg.essential.api.EssentialAPI
import gg.essential.universal.UChat
import gg.essential.universal.UDesktop
import gg.essential.universal.UKeyboard
import gg.skytils.event.EventSubscriber
import gg.skytils.skytilsmod.commands.impl.*
import gg.skytils.skytilsmod.commands.stats.impl.CataCommand
import gg.skytils.skytilsmod.commands.stats.impl.SlayerCommand
import gg.skytils.skytilsmod.core.*
import gg.skytils.skytilsmod.tweaker.DependencyLoader
import gg.skytils.skytilsmod.events.impl.MainReceivePacketEvent
import gg.skytils.skytilsmod.events.impl.PacketEvent
import gg.skytils.skytilsmod.features.impl.crimson.KuudraChestProfit
import gg.skytils.skytilsmod.features.impl.crimson.KuudraFeatures
import gg.skytils.skytilsmod.features.impl.crimson.TrophyFish
import gg.skytils.skytilsmod.features.impl.dungeons.*
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.Catlas
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.CatlasConfig
import gg.skytils.skytilsmod.features.impl.dungeons.solvers.*
import gg.skytils.skytilsmod.features.impl.dungeons.solvers.terminals.*
import gg.skytils.skytilsmod.features.impl.events.GriffinBurrows
import gg.skytils.skytilsmod.features.impl.events.MayorDiana
import gg.skytils.skytilsmod.features.impl.events.MayorJerry
import gg.skytils.skytilsmod.features.impl.events.TechnoMayor
import gg.skytils.skytilsmod.features.impl.farming.FarmingFeatures
import gg.skytils.skytilsmod.features.impl.farming.GardenFeatures
import gg.skytils.skytilsmod.features.impl.farming.TreasureHunter
import gg.skytils.skytilsmod.features.impl.farming.VisitorHelper
import gg.skytils.skytilsmod.features.impl.funny.Funny
import gg.skytils.skytilsmod.features.impl.handlers.*
import gg.skytils.skytilsmod.features.impl.mining.MiningFeatures
import gg.skytils.skytilsmod.features.impl.mining.StupidTreasureChestOpeningThing
import gg.skytils.skytilsmod.features.impl.misc.*
import gg.skytils.skytilsmod.features.impl.overlays.AuctionPriceOverlay
import gg.skytils.skytilsmod.features.impl.protectitems.ProtectItems
import gg.skytils.skytilsmod.features.impl.slayer.SlayerFeatures
import gg.skytils.skytilsmod.features.impl.spidersden.RainTimer
import gg.skytils.skytilsmod.features.impl.spidersden.RelicWaypoints
import gg.skytils.skytilsmod.features.impl.spidersden.SpidersDenFeatures
import gg.skytils.skytilsmod.features.impl.trackers.impl.DupeTracker
import gg.skytils.skytilsmod.features.impl.trackers.impl.MayorJerryTracker
import gg.skytils.skytilsmod.features.impl.trackers.impl.MythologicalTracker
import gg.skytils.skytilsmod.gui.OptionsGui
import gg.skytils.skytilsmod.gui.ReopenableGUI
import gg.skytils.skytilsmod.listeners.ChatListener
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.localapi.LocalAPI
import gg.skytils.skytilsmod.mixins.extensions.ExtensionEntityLivingBase
import gg.skytils.skytilsmod.mixins.hooks.entity.EntityPlayerSPHook
import gg.skytils.skytilsmod.mixins.hooks.util.MouseHelperHook
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorCommandHandler
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiStreamUnavailable
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorSettingsGui
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.colors.CustomColor
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiGameOver
import net.minecraft.client.gui.GuiIngameMenu
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.settings.KeyBinding
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.ContainerPlayer
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.server.S01PacketJoinGame
import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraft.network.play.server.S3DPacketDisplayScoreboard
import net.minecraft.network.play.server.S3FPacketCustomPayload
import sun.misc.Unsafe
import java.io.File
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.security.KeyStore
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs

//#if FORGE
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.common.MinecraftForge
//#if MC<11400
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
//#else
//$$ import net.minecraft.util.text.StringTextComponent
//#endif
//#endif

//#if FABRIC
//$$ import net.fabricmc.loader.api.FabricLoader
//$$ import net.minecraft.client.gui.widget.ButtonWidget
//$$ import net.minecraft.network.packet.BrandCustomPayload
//$$ import net.minecraft.text.Text
//#endif

//#if FORGE
//#if MC<11400
@Mod(
    modid = Skytils.MOD_ID,
    name = Skytils.MOD_NAME,
    acceptedMinecraftVersions = "[1.8.9]",
    clientSideOnly = true,
    guiFactory = "gg.skytils.skytilsmod.core.ForgeGuiFactory",
    modLanguageAdapter = "gg.essential.api.utils.KotlinAdapter"
)
//#else
//$$ @Mod(Skytils.MOD_ID)
//#endif
//#endif
object Skytils : CoroutineScope {
    const val MOD_ID = Reference.MOD_ID
    const val MOD_NAME = Reference.MOD_NAME
    @JvmField
    val VERSION = Reference.VERSION

    @JvmStatic
    val mc: Minecraft by lazy {
        Minecraft.getMinecraft()
    }

    @JvmStatic
    val config by lazy {
        Config
    }

    val modDir by lazy {
        File(File(mc.mcDataDir, "config"), "skytils").also {
            it.mkdirs()
            File(it, "trackers").mkdirs()
        }
    }

    @JvmStatic
    lateinit var guiManager: GuiManager

    @JvmField
    val sendMessageQueue = ArrayDeque<String>()

    @JvmField
    var usingLabymod = false

    @JvmField
    var usingNEU = false

    @JvmField
    var usingSBA = false

    @JvmField
    var jarFile: File? = null
    private var lastChatMessage = 0L

    @JvmField
    var displayScreen: GuiScreen? = null

    @JvmField
    val threadPool = Executors.newFixedThreadPool(10) as ThreadPoolExecutor

    @JvmField
    val dispatcher = threadPool.asCoroutineDispatcher()

    val IO = object : CoroutineScope {
        override val coroutineContext = Dispatchers.IO + SupervisorJob() + CoroutineName("Skytils IO")
    }

    override val coroutineContext: CoroutineContext = dispatcher + SupervisorJob() + CoroutineName("Skytils")

    val deobfEnvironment: Boolean
        get() = isDeobfuscatedEnvironment

    val unsafe by lazy {
        Unsafe::class.java.getDeclaredField("theUnsafe").apply {
            isAccessible = true
        }.get(null) as Unsafe
    }

    @JvmStatic
    val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            include(serializersModule)
            contextual(CustomColor::class, CustomColor.Serializer)
            contextual(Regex::class, RegexAsString)
            contextual(UUID::class, UUIDAsString)
        }
    }

    val client = HttpClient(CIO) {
        install(ContentEncoding) {
            customEncoder(BrotliEncoder, 1.0F)
            deflate(1.0F)
            gzip(0.9F)
            identity(0.1F)
        }
        install(ContentNegotiation) {
            json(json)
            json(json, ContentType.Text.Plain)
        }
        install(HttpCache)
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            exponentialDelay()
        }
        install(HttpTimeout)
        install(UserAgent) {
            agent = "Skytils/$VERSION"
        }

        engine {
            endpoint {
                connectTimeout = 10000
                keepAliveTime = 5000
                requestTimeout = 10000
                socketTimeout = 10000
            }
            https {
                val backingManager = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
                    init(null as KeyStore?)
                }.trustManagers.first { it is X509TrustManager } as X509TrustManager

                val ourManager = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
                    Skytils::class.java.getResourceAsStream("/skytilscacerts.jks").use {
                        val ourKs = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                            load(it, "skytilsontop".toCharArray())
                        }
                        init(ourKs)
                    }
                }.trustManagers.first { it is X509TrustManager } as X509TrustManager

                trustManager = UnionX509TrustManager(backingManager, ourManager)
            }
        }
    }

    val areaRegex = Regex("§r§b§l(?<area>[\\w]+): §r§7(?<loc>[\\w ]+)§r")

    var domain = "api.skytils.gg"

    val prefix = "§9§lSkytils §8»"
    val successPrefix = "§a§lSkytils §8»"
    val failPrefix = "§c§lSkytils (${Reference.VERSION}) §8»"

    var trustClientTime = false

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        DataFetcher.preload()
        guiManager = GuiManager
        //#if FORGE
        jarFile = Loader.instance().modList.find { it.modId == MOD_ID }?.source
        mc.framebuffer.enableStencil()
        //#else
        //$$ jarFile = FabricLoader.getInstance().allMods.find { it.metadata.id == MOD_ID }?.origin?.paths?.firstOrNull()?.toFile()
        //#endif

        config.init()
        CatlasConfig
        UpdateChecker.downloadDeleteTask()

        arrayOf(
            this,
            ChatListener,
            DungeonListener,
            guiManager,
            LocalAPI,
            MayorInfo,
            SBInfo,
            UpdateChecker,

            AlignmentTaskSolver,
            AntiFool,
            ArmorColor,
            AuctionData,
            AuctionPriceOverlay,
            BetterStash,
            BlazeSolver,
            BloodHelper,
            BrewingFeatures,
            BossHPDisplays,
            BoulderSolver,
            ChatTabs,
            ChangeAllToSameColorSolver,
            DungeonChestProfit,
            ClickInOrderSolver,
            NamespacedCommands,
            CreeperSolver,
            CommandAliases,
            ContainerSellValue,
            CooldownTracker,
            CustomNotifications,
            DamageSplash,
            DungeonFeatures,
            Catlas,
            DungeonTimer,
            DupeTracker,
            EnchantNames,
            FarmingFeatures,
            FavoritePets,
            Funny,
            GardenFeatures,
            GlintCustomizer,
            GriffinBurrows,
            IceFillSolver,
            IcePathSolver,
            ItemCycle,
            ItemFeatures,
            KeyShortcuts,
            KuudraChestProfit,
            KuudraFeatures,
            LividFinder,
            LockOrb,
            MasterMode7Features,
            MayorDiana,
            MayorJerry,
            MayorJerryTracker,
            MiningFeatures,
            MinionFeatures,
            MiscFeatures,
            MythologicalTracker,
            PartyAddons,
            PartyFeatures,
            PartyFinderStats,
            PetFeatures,
            Ping,
            PotionEffectTimers,
            PricePaid,
            ProtectItems,
            QuiverStuff,
            RainTimer,
            RandomStuff,
            RelicWaypoints,
            ScamCheck,
            ScoreCalculation,
            SelectAllColorSolver,
            ShootTheTargetSolver,
            SimonSaysSolver,
            SlayerFeatures,
            SpidersDenFeatures,
            SpamHider,
            SpiritLeap,
            StartsWithSequenceSolver,
            StupidTreasureChestOpeningThing,
            TankDisplayStuff,
            TechnoMayor,
            TeleportMazeSolver,
            TerminalFeatures,
            ThreeWeirdosSolver,
            TicTacToeSolver,
            TreasureHunter,
            TriviaSolver,
            TrophyFish,
            VisitorHelper,
            WaterBoardSolver,
            Waypoints,
            EntityPlayerSPHook,
            MouseHelperHook
        ).forEach(MinecraftForge.EVENT_BUS::register)

        arrayOf(
            SoundQueue
        ).forEach(EventSubscriber::setup)
    }

    @Mod.EventHandler
    fun loadComplete(event: FMLLoadCompleteEvent) {
        usingLabymod = isModLoaded("labymod")
        usingNEU = isModLoaded("notenoughupdates")
        usingSBA = isModLoaded("skyblockaddons")

        MayorInfo.fetchMayorData()

        PersistentSave.loadData()

        ModChecker.checkModdedForge()

        ScreenRenderer.init()


        val cch = ClientCommandHandler.instance

        if (cch !is AccessorCommandHandler) throw RuntimeException(
            "Skytils was unable to mixin to the CommandHandler. Please report this on our Discord at discord.gg/skytils."
        )
        cch.registerCommand(SkytilsCommand)

        cch.registerCommand(CataCommand)
        cch.registerCommand(CalcXPCommand)
        cch.registerCommand(FragBotCommand)
        cch.registerCommand(HollowWaypointCommand)
        cch.registerCommand(ItemCycleCommand)
        cch.registerCommand(LimboCommand)
        cch.registerCommand(OrderedWaypointCommand)
        cch.registerCommand(ScamCheckCommand)
        cch.registerCommand(SlayerCommand)
        cch.registerCommand(TrophyFishCommand)

        if (!cch.commands.containsKey("armorcolor")) {
            cch.registerCommand(ArmorColorCommand)
        }

        if (!cch.commands.containsKey("glintcustomize")) {
            cch.registerCommand(GlintCustomizeCommand)
        }

        if (!cch.commands.containsKey("protectitem")) {
            cch.registerCommand(ProtectItemCommand)
        }

        if (!cch.commands.containsKey("trackcooldown")) {
            cch.registerCommand(TrackCooldownCommand)
        }

        cch.commandSet.add(RepartyCommand)
        cch.commandMap["skytilsreparty"] = RepartyCommand
        if (config.overrideReparty || !cch.commands.containsKey("reparty")) {
            cch.commandMap["reparty"] = RepartyCommand
        }

        if (config.overrideReparty || !cch.commands.containsKey("rp")) {
            cch.commandMap["rp"] = RepartyCommand
        }

        if (UpdateChecker.currentVersion.specialVersionType != UpdateChecker.UpdateType.RELEASE && config.updateChannel == 2) {
            if (ModChecker.canShowNotifications) {
                EssentialAPI.getNotifications().push("Skytils Update Checker", "You are on a development version of Skytils. Click here to change your update channel to pre-release.") {
                    onAction = {
                        config.updateChannel = 1
                        config.markDirty()
                        EssentialAPI.getNotifications().push("Skytils Update Checker", "Your update channel has been changed to pre-release.", duration = 3f)
                    }
                }
            } else {
                UChat.chat("$prefix §fYou are on a development version of Skytils. Change your update channel to pre-release to get notified of new updates.")
            }
        }

        checkSystemTime()

        if (!DependencyLoader.hasNativeBrotli) {
            if (ModChecker.canShowNotifications) {
                EssentialAPI.getNotifications().push("Skytils Warning", "Native Brotli is not available. Skytils will use the Java Brotli decoder, which cannot encode Brotli.", duration = 3f)
            } else {
                UChat.chat("$prefix §fNative Brotli is not available. Skytils will use the Java Brotli decoder, which cannot encode Brotli.")
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return

        ScreenRenderer.refresh()
        EntityManager.tickEntities()

        ScoreboardUtil.sidebarLines = ScoreboardUtil.fetchScoreboardLines().map { ScoreboardUtil.cleanSB(it) }
        TabListUtils.tabEntries = TabListUtils.fetchTabEntries().map { it to it.text }
        if (displayScreen != null) {
            if (mc.thePlayer?.openContainer is ContainerPlayer) {
                mc.displayGuiScreen(displayScreen)
                displayScreen = null
            }
        }

        if (mc.thePlayer != null && sendMessageQueue.isNotEmpty() && System.currentTimeMillis() - lastChatMessage > 250) {
            val msg = sendMessageQueue.pollFirst()
            if (!msg.isNullOrBlank()) UChat.say(msg)
        }
        if (Utils.inSkyblock && DevTools.getToggle("copydetails") && UKeyboard.isCtrlKeyDown()) {
            if (UKeyboard.isKeyDown(UKeyboard.KEY_TAB)) {
                UChat.chat("Copied tab data to clipboard")
                UDesktop.setClipboardString(TabListUtils.tabEntries.map { it.second }.toString())
            }
            if (UKeyboard.isKeyDown(UKeyboard.KEY_CAPITAL)) {
                UChat.chat("Copied scoreboard data to clipboard")
                UDesktop.setClipboardString(ScoreboardUtil.sidebarLines.toString())
            }
            val openScreen = mc.currentScreen
            if (UKeyboard.isKeyDown(UKeyboard.KEY_LMETA) && openScreen is GuiChest) {
                //#if MC<11400
                val container = openScreen.inventorySlots
                //#else
                //$$ val container = openScreen.screenHandler
                //#endif
                (container as? ContainerChest)?.let { chest ->
                    UChat.chat("Copied container data to clipboard")
                    //#if MC<11400
                    val name = chest.lowerChestInventory.name
                    //#else
                    //$$ val name = openScreen.title.string
                    //#endif
                    UDesktop.setClipboardString(
                        "Name: '${name}', Items: ${
                            chest.inventorySlots.filter { it.inventory == chest.lowerChestInventory }
                                .map { it.stack?.serializeNBT() }
                        }"
                    )

                }
            }
        }
    }

    init {
        tickTimer(20, repeats = true) {
            if (mc.thePlayer != null) {
                if (deobfEnvironment) {
                    if (DevTools.toggles.getOrDefault("forcehypixel", false)) Utils.isOnHypixel = true
                    if (DevTools.toggles.getOrDefault("forceskyblock", false)) Utils.skyblock = true
                    if (DevTools.toggles.getOrDefault("forcedungeons", false)) Utils.dungeons = true
                }
                if (DevTools.getToggle("sprint")) {
                    //#if MC<11400
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.keyCode, true)
                    //#else
                    //$$ mc.options.sprintKey?.isPressed = true
                    //#endif

                }
            }
        }
    }

    @SubscribeEvent
    fun onConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        IO.launch {
            TrophyFish.loadFromApi()
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onPacket(event: MainReceivePacketEvent<*, *>) {
        if (event.packet is S01PacketJoinGame) {
            Utils.skyblock = false
            Utils.dungeons = false
        }
        if (!Utils.inSkyblock && Utils.isOnHypixel && event.packet is S3DPacketDisplayScoreboard && event.packet.func_149371_c() == 1) {
            Utils.skyblock = event.packet.func_149370_d() == "SBScoreboard"
            printDevMessage("score ${event.packet.func_149370_d()}", "utils")
            printDevMessage("sb ${Utils.inSkyblock}", "utils")
        }
        if (event.packet is S1CPacketEntityMetadata && mc.thePlayer != null) {
            val nameObj = event.packet.func_149376_c()?.find { it.dataValueId == 2 }?.`object` ?: return
            val entity = mc.theWorld?.getEntityByID(event.packet.entityId)

            if (entity is ExtensionEntityLivingBase) {
                entity.skytilsHook.onNewDisplayName(nameObj as String)
            }
        }
        if (!Utils.isOnHypixel && event.packet is S3FPacketCustomPayload) {
            //#if MC<11400
            if (event.packet.channelName.toString() == "MC|Brand") {
                Utils.isOnHypixel = event.packet.bufferData.readStringFromBuffer(Short.MAX_VALUE.toInt()).lowercase().contains("hypixel")
            }
            //#else
            //$$ Utils.isOnHypixel = (event.packet.payload as? BrandCustomPayload)?.brand?.contains("hypixel") == true
            //#endif
        }
    }

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        Utils.isOnHypixel = false
        Utils.skyblock = false
        Utils.dungeons = false
    }

    @SubscribeEvent
    fun onSendPacket(event: PacketEvent.SendEvent) {
        if (event.packet is C01PacketChatMessage) {
            lastChatMessage = System.currentTimeMillis()
        }
    }

    @SubscribeEvent
    fun onRenderGameOverlay(event: RenderGameOverlayEvent) {
        if (mc.currentScreen is OptionsGui && event.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onGuiInitPost(event: GuiScreenEvent.InitGuiEvent.Post) {
        if (config.configButtonOnPause && event.gui is GuiIngameMenu) {
            val x = event.gui.width - 105
            val x2 = x + 100
            var y = event.gui.height - 22
            var y2 = y + 20
            //#if MC<11400
            val sorted = event.buttonList.sortedWith { a, b -> b.yPosition + b.height - a.yPosition + a.height }
            //#else
            //$$ val sorted = event.widgetList.sortedWith { a, b -> b.y + b.heightRealms - a.y + a.heightRealms }
            //#endif
            for (button in sorted) {
                val otherX = button.xPosition
                val otherX2 = button.xPosition + button.width
                val otherY = button.yPosition
                val otherY2 = button.yPosition + button.height
                if (otherX2 > x && otherX < x2 && otherY2 > y && otherY < y2) {
                    y = otherY - 20 - 2
                    y2 = y + 20
                }
            }
            //#if MC<11400
            event.buttonList.add(GuiButton(6969420, x, 0.coerceAtLeast(y), 100, 20, "Skytils"))
            //#else
            //$$ event.widgetList.add(
            //$$     ButtonWidget
            //$$         .Builder(Text.of("Skytils")) {
            //$$             displayScreen = OptionsGui()
            //$$         }
            //$$         .position(x, 0.coerceAtLeast(y))
            //$$         .build()
            //$$ )
            //#endif
        }
    }

    //#if MC<11400
    @SubscribeEvent
    fun onGuiAction(event: GuiScreenEvent.ActionPerformedEvent.Post) {
        if (config.configButtonOnPause && event.gui is GuiIngameMenu && event.button.id == 6969420) {
            displayScreen = OptionsGui()
        }
    }
    //#endif

    @SubscribeEvent
    fun onGuiChange(event: GuiOpenEvent) {
        val old = mc.currentScreen
        if (event.gui == null && old is OptionsGui && old.parent != null) {
            displayScreen = old.parent
        } else if (event.gui == null && config.reopenOptionsMenu) {
            if (old is ReopenableGUI || (old is AccessorSettingsGui && old.config is Config)) {
                tickTimer(1) {
                    if (mc.thePlayer?.openContainer is ContainerPlayer)
                        displayScreen = OptionsGui()
                }
            }
        }
        if (old is AccessorGuiStreamUnavailable) {
            if (config.twitchFix && event.gui == null && !(Utils.inSkyblock && old.parentScreen is GuiGameOver)) {
                event.gui = old.parentScreen
            }
        }
    }

    private fun checkSystemTime() {
        IO.launch {
            DatagramSocket().use { socket ->
                val address = InetAddress.getByName("time.nist.gov")
                val buffer = NtpMessage().toByteArray()
                var packet = DatagramPacket(buffer, buffer.size, address, 123)
                socket.send(packet)
                packet = DatagramPacket(buffer, buffer.size)

                val destinationTimestamp = NtpMessage.now()
                val msg = NtpMessage(packet.data)

                val localClockOffset =
                    ((msg.receiveTimestamp - msg.originateTimestamp) +
                            (msg.transmitTimestamp - destinationTimestamp)) / 2

                println("Got local clock offset: $localClockOffset")
                if (abs(localClockOffset) > 3) {
                    if (ModChecker.canShowNotifications) {
                        EssentialAPI.getNotifications().push("Skytils", "Your system time is inaccurate.", 3f)
                    } else {
                        UChat.chat("$prefix §fYour system time appears to be inaccurate. Please sync your system time to avoid issues with Skytils.")
                    }
                } else {
                    trustClientTime = true
                }
            }
        }
    }
}