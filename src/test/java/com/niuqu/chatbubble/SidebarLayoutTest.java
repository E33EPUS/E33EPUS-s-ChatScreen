package com.niuqu.chatbubble;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SidebarLayoutTest {

    @Test
    void clampsScrollToZeroWhenNoPlayers() {
        var layout = SidebarLayout.create(200, 50, 30, 60, 0);
        assertEquals(0, layout.scroll());
    }

    @Test
    void clampsScrollToMax() {
        // 3 players * 22 row = 66, viewport = 200-60=140, max = max(0, 66-140) = 0
        var layout = SidebarLayout.create(200, 999, 30, 60, 3);
        assertEquals(0, layout.scroll());
    }

    @Test
    void scrollAllowedWhenManyPlayers() {
        // 20 players * 22 = 440, viewport = 200-60=140, max = 440-140 = 300
        var layout = SidebarLayout.create(200, 150, 30, 60, 20);
        assertEquals(150, layout.scroll());
    }

    @Test
    void hitPublicButton() {
        var layout = SidebarLayout.create(200, 0, 30, 60, 5);
        assertEquals(SidebarLayout.PUBLIC, layout.hit(35)); // within publicTop..publicTop+20
    }

    @Test
    void hitPlayerRow() {
        var layout = SidebarLayout.create(200, 0, 30, 60, 5);
        assertEquals(0, layout.hit(65));  // first row at playersTop
        assertEquals(1, layout.hit(87));  // second row
    }

    @Test
    void hitNoneAbovePublic() {
        var layout = SidebarLayout.create(200, 0, 30, 60, 5);
        assertEquals(SidebarLayout.NONE, layout.hit(10));
    }

    @Test
    void hitNoneBeyondPlayerCount() {
        var layout = SidebarLayout.create(200, 0, 30, 60, 2);
        assertEquals(SidebarLayout.NONE, layout.hit(200)); // way below
    }

    @Test
    void hitAccountsForScroll() {
        // 20 players, scrolled 44px (2 rows)
        var layout = SidebarLayout.create(200, 44, 30, 60, 20);
        assertEquals(2, layout.hit(60)); // at playersTop, scrolled 2 rows → index 2
    }
}
