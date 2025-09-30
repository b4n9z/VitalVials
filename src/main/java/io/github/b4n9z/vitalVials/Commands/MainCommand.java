package io.github.b4n9z.vitalVials.Commands;

import io.github.b4n9z.vitalVials.VitalVials;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MainCommand implements CommandExecutor {
    private final VitalVials plugin;
    private final ReloadPluginCommand reloadPluginCommand;
    private final ShopCommand shopCommand;
    private final BuyEffectCommand buyEffectCommand;
    private final ScoreboardCommand scoreBoardCommand;
    private final ViewStatusCommand viewStatusCommand;
    private final RefundEffectCommand refundEffectCommand;
    private final RemoveDataCommand removeDataCommand;

    public MainCommand(VitalVials plugin) {
        this.plugin = plugin;
        this.reloadPluginCommand = new ReloadPluginCommand(this.plugin);
        this.shopCommand = new ShopCommand(this.plugin);
        this.buyEffectCommand = new BuyEffectCommand(this.plugin);
        this.scoreBoardCommand = new ScoreboardCommand(this.plugin);
        this.viewStatusCommand = new ViewStatusCommand(this.plugin);
        this.refundEffectCommand = new RefundEffectCommand(this.plugin);
        this.removeDataCommand = new RemoveDataCommand(this.plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cPlease specify a subcommand.");
            return false;
        }

        String subCommand = args[0];
        return switch (subCommand) {
            case "reload" -> reloadPluginCommand.onCommand(sender, command, label, args);
            case "scoreboard" -> scoreBoardCommand.onCommand(sender, command, label, args);
            case "shop" -> shopCommand.onCommand(sender, command, label, args);
            case "buyEffect" -> buyEffectCommand.onCommand(sender, command, label, args);
            case "confirmBuy" -> buyEffectCommand.confirmBuyCommand(sender, args);
            case "cancelBuy" -> buyEffectCommand.cancelBuyCommand(sender, args);
            case "viewStatus" -> viewStatusCommand.onCommand(sender, command, label, args);
            case "refundEffect" -> refundEffectCommand.onCommand(sender, command, label, args);
            case "confirmRefund" -> refundEffectCommand.confirmRefundCommand(sender, args);
            case "cancelRefund" -> refundEffectCommand.cancelRefundCommand(sender, args);
            case "removeData" -> removeDataCommand.onCommand(sender, command, label, args);
            case "confirmRemoveData" -> removeDataCommand.confirmRemoveData(sender, args);
            case "confirmRemoveAllData" -> removeDataCommand.confirmRemoveAllData(sender, args);
            case "cancelRemoveData" -> removeDataCommand.cancelRemoveData(sender, args);
            default -> {
                sender.sendMessage("§cUnknown subcommand.");
                yield true;
            }
        };
    }
}
