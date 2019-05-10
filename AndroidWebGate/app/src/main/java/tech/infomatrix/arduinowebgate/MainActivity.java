
package tech.infomatrix.arduinowebgate;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.SocketException;

import static java.lang.System.out;


public class MainActivity extends AppCompatActivity {

    private Button btnStartStop;
    private TextView tvFeedback;
    private TextView tvIPAddress;
    private WebBox webBox;
    private UartGate uartGate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Hello, zero", Snackbar.LENGTH_LONG)
                        .setAction("Hello, one", null).show();
            }
        });

        /* - - */
        this.tvFeedback = (TextView) findViewById(R.id.tvFeedback);
        this.tvIPAddress = (TextView) findViewById(R.id.tvIPAddress);
        this.btnStartStop = (Button) findViewById(R.id.btnStart);

        /* - - */
        try {

            /* - - */
            WebBox.ctx = this.getApplicationContext();
            this.webBox = new WebBox();

            /* - - */
            UartGate.ctx = this.getApplicationContext();
            this.uartGate = new UartGate();

            if (this.webBox.isSetup())
                out.println(" +++ setup done +++ ");
            else
                this.webBox.runSetup();

        } catch (IOException e) {
            out.println(e.toString());
        } catch (Exception e) {
            out.println(e.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* button click button */
    public void btnStartClick(View v) {
        /* - - */
        this.tvFeedback.setText("Running...");
        /* start server */
        try {
            this.webBox.startAdminServer();
            if (this.webBox.setLocalAddress())
                this.tvIPAddress.setText(String.format("http://%s:%s/", WebBox.ipAddress, WebBox.adminPort));
            this.btnStartStop.setText("Stop Admin Server");
            /* refresh */
            v.invalidate();
        } catch (Exception e) {
            out.println("e: " + e.toString());
        }
    }

}
