package ml.karmaconfigs.lockloginsystem.spigot.events;

import ml.karmaconfigs.lockloginsystem.spigot.utils.StringUtils;
import ml.karmaconfigs.lockloginsystem.spigot.utils.inventory.Numbers;
import ml.karmaconfigs.lockloginsystem.spigot.utils.inventory.PinInventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

/*
GNU LESSER GENERAL PUBLIC LICENSE
                       Version 2.1, February 1999

 Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.

[This is the first released version of the Lesser GPL.  It also counts
 as the successor of the GNU Library Public License, version 2, hence
 the version number 2.1.]
 */

public final class InventoryEventHandler implements Listener {

    /**
     * Check if the inventory is
     * pin GUI
     *
     * @param title the title
     * @return if the title matches with
     * LockLogin pinner
     */
    private boolean isPinGUI(String title) {
        String check = "LockLogin pinner";

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < title.length(); i++) {
            String letter = String.valueOf(title.charAt(i));
            if (letter.matches(".*[aA-zZ].*") || letter.matches(".*[0-9].*") || letter.matches(" ")) {
                builder.append(letter);
            }
        }

        title = builder.toString();

        builder = new StringBuilder();
        for (int i = 0; i < check.length(); i++) {
            String letter = String.valueOf(check.charAt(i));
            if (letter.matches(".*[aA-zZ].*") || letter.matches(".*[0-9].*") || letter.matches(" ") && !letter.matches("&" + ".*[aA-zZ]") && !letter.matches("&" + ".*[0-9].*")) {
                builder.append(letter);
            }
        }
        check = builder.toString();

        return title.contains(check);
    }

    /**
     * Check if the clicked item is a number
     *
     * @param clicked the clicked item
     * @return if the clicked stack is a number
     */
    private boolean isNumber(ItemStack clicked) {
        if (clicked.hasItemMeta()) {
            if (clicked.getItemMeta().hasDisplayName()) {
                String name = clicked.getItemMeta().getDisplayName();

                return name.contains("0")
                        || name.contains("1")
                        || name.contains("2")
                        || name.contains("3")
                        || name.contains("4")
                        || name.contains("5")
                        || name.contains("6")
                        || name.contains("7")
                        || name.contains("8")
                        || name.contains("9");
            }
        }
        return false;
    }

    /**
     * Check if the clicked item is similar
     * to the one to check with
     *
     * @param clicked the clicked item
     * @param check   the one to check with
     * @return if the clicked stack is similar to the "check" one
     */
    private boolean isSimilar(ItemStack clicked, ItemStack check) {
        boolean isSimilar = false;
        if (clicked.hasItemMeta()) {
            if (clicked.getItemMeta().hasDisplayName()) {
                if (check.hasItemMeta()) {
                    if (check.getItemMeta().hasDisplayName()) {
                        if (StringUtils.toColor(clicked.getItemMeta().getDisplayName()).equals(StringUtils.toColor(check.getItemMeta().getDisplayName()))) {
                            isSimilar = true;
                        }
                    }
                }
            }
        }
        return isSimilar;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player player = (Player) e.getWhoClicked();

            InventoryView view = e.getView();

            if (view != null) {
                if (view.getTitle() != null && !view.getTitle().isEmpty()) {
                    if (isPinGUI(view.getTitle())) {
                        PinInventory inventory = new PinInventory(player);

                        ItemStack clicked = e.getCurrentItem();

                        if (clicked != null && !clicked.getType().equals(Material.AIR)) {
                            if (isSimilar(clicked, Numbers.getEraser())) {
                                inventory.eraseInput();
                            } else {
                                if (isSimilar(clicked, Numbers.getConfirm())) {
                                    inventory.confirm();
                                } else {
                                    if (isNumber(clicked)) {
                                        int number = 0;
                                        if (isSimilar(clicked, Numbers.getOne())) {
                                            number = 1;
                                        }
                                        if (isSimilar(clicked, Numbers.getTwo())) {
                                            number = 2;
                                        }
                                        if (isSimilar(clicked, Numbers.getThree())) {
                                            number = 3;
                                        }
                                        if (isSimilar(clicked, Numbers.getFour())) {
                                            number = 4;
                                        }
                                        if (isSimilar(clicked, Numbers.getFive())) {
                                            number = 5;
                                        }
                                        if (isSimilar(clicked, Numbers.getSix())) {
                                            number = 6;
                                        }
                                        if (isSimilar(clicked, Numbers.getSeven())) {
                                            number = 7;
                                        }
                                        if (isSimilar(clicked, Numbers.getEight())) {
                                            number = 8;
                                        }
                                        if (isSimilar(clicked, Numbers.getNine())) {
                                            number = 9;
                                        }
                                        if (isSimilar(clicked, Numbers.getZero())) {
                                            number = 0;
                                        }

                                        inventory.setInput(String.valueOf(number));
                                        inventory.updateInput();
                                    }
                                }
                            }
                        }
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
}
