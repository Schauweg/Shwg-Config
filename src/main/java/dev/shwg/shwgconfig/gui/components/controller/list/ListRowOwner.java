package dev.shwg.shwgconfig.gui.components.controller.list;

import java.util.List;

/**
 * What a {@link ListRow} needs from the list controller it belongs to.
 * Internal - implemented by the list controller only.
 */
interface ListRowOwner<E> {

    /**
     * A row committed a new element: rebuild and save the list.
     */
    void rowChanged(ListRow<E> row);

    /**
     * The committed elements of every row except {@code row}, in list order.
     */
    List<E> valuesExcept(ListRow<E> row);
}
