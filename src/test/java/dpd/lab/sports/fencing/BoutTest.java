package dpd.lab.sports.fencing;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static dpd.lab.sports.fencing.BoutStatus.NOT_STARTED;
import static dpd.lab.sports.fencing.BoutStatus.ONGOING;
import static org.assertj.core.api.Assertions.assertThat;

public class BoutTest {

    private static Fencer firstBoutFencer;

    private static Fencer secondBoutFencer;

    @BeforeAll
    public static void setUp() {
        firstBoutFencer = new Fencer("Anthony Perkins");
        secondBoutFencer = new Fencer("Dorothy Wallace");
    }

    @Test
    public void shouldInstantiateBoutCorrectly() {
        Bout bout = new Bout(firstBoutFencer, secondBoutFencer);

        assertThat(bout.getFencers()).hasSize(2).contains(firstBoutFencer, secondBoutFencer);
        assertThat(bout.getStatus()).isEqualTo(NOT_STARTED);
    }

    @Test
    public void shouldStartAndProgressBoutCorrectly() {
        Bout bout = new Bout(firstBoutFencer, secondBoutFencer);
        bout.addPoint(firstBoutFencer);
        bout.addPoint(firstBoutFencer);
        bout.addPoint(firstBoutFencer);
        bout.addPoint(secondBoutFencer);
        bout.addPoint(secondBoutFencer);

        assertThat(bout.getStatus()).isEqualTo(ONGOING);
        assertThat(bout.getScore(firstBoutFencer)).isEqualTo(3);
        assertThat(bout.getScore(secondBoutFencer)).isEqualTo(2);
    }

    @Test
    public void shouldConcludeBoutCorrectly() {
        Bout bout = new Bout(firstBoutFencer, secondBoutFencer);
        bout.addPoint(firstBoutFencer);
        bout.addPoint(firstBoutFencer);
        bout.addPoint(firstBoutFencer);
        bout.addPoint(secondBoutFencer);
        bout.addPoint(secondBoutFencer);
        bout.addPoint(firstBoutFencer);
        bout.addPoint(secondBoutFencer);
        bout.addPoint(firstBoutFencer);
        bout.conclude();
        assertThat(bout.getScore(firstBoutFencer)).isEqualTo(5);
        assertThat(bout.getScore(secondBoutFencer)).isEqualTo(3);
    }
}
