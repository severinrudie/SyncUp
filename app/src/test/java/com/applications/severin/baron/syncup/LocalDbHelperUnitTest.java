package com.applications.severin.baron.syncup;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.applications.severin.baron.syncup.DataModels.Event;
import com.applications.severin.baron.syncup.DataModels.Invitation;
import com.applications.severin.baron.syncup.DataModels.Note;
import com.applications.severin.baron.syncup.Database.LocalDbHelper;
import com.applications.severin.baron.syncup.Database.PH;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by erikrudie on 11/22/16.
 */

@RunWith(RobolectricGradleTestRunner.class)
// To use Robolectric you'll need to setup some constants.
// Change it according to your needs.
@Config(constants = BuildConfig.class, sdk = 21, manifest = "/src/main/AndroidManifest.xml")
public class LocalDbHelperUnitTest {

  private Context mContext;
  private LocalDbHelper mHelper;
  private SQLiteDatabase mDb;

  private Event testEvent;

  private long eventId;
  private int ownerId;
  private String name;
  private String location;
  private Bitmap pictureMedium;
  private Bitmap pictureSmall;
  private long fromTime;
  private long toTime;
  private List<Note> notes;
  private List<Invitation> invitations;

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @Before
  public void setup() {
    mContext = RuntimeEnvironment.application;
    mHelper = new LocalDbHelper(mContext, true);
    mDb = mHelper.getWritableDatabase();

    // Set up sample Event
    eventId = 1;
    ownerId = 2;
    name = "Fred";
    location = "Seattle";
    pictureMedium = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.cookie_small);
    pictureSmall = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.cookie_small);
    fromTime = 3;
    toTime = 4;
    notes = new ArrayList<>();
    notes.add(new Note(eventId, ownerId, "my note"));
    invitations = new ArrayList<>();
    invitations.add(Invitation.inflateInvitationFromDb(1, 2, 3, Invitation.INVITED));

    testEvent = new Event.EventBuilder().setEventId(eventId).setOwnerId(ownerId)
      .setName(name).setLocation(location).setPictureMedium(pictureMedium)
      .setPictureSmall(pictureSmall).setFromTime(fromTime).setToTime(toTime).setNotes(notes)
      .setInvitations(invitations).build();
  }

  @After
  public void cleanup() {
    mDb.close();
  }

  @Test
  public void db_shouldBeCreated() {
    // Verify is the DB is opening correctly
    assertTrue("DB didn't open", mDb.isOpen());
  }

  @Test
  public void eventTable_shouldHaveProperColumns() {
    String sql = "SELECT * FROM " + PH.TABLE_EVENT;
    Cursor cursor = mDb.rawQuery(sql, null);
    String[] columns = cursor.getColumnNames();

    String[] projectColumns = {PH.EVENT_ID, PH.OWNER_ID, PH.NAME,
      PH.PICTURE_MEDIUM, PH.PICTURE_SMALL, PH.LOCATION,
      PH.FROM_TIME, PH.TO_TIME};
    for (String column : projectColumns) {
      assertThat("Column not implemented: " + column,
        columns,
        hasItemInArray(column));
      Assert.assertThat("False positives are a bad thing: ",
        columns,
        not(hasItemInArray("anUnlikelyColumn")));
    }
    cursor.close();
  }

  @Test
  public void noteTable_shouldHaveProperColumns() {
    String sql = "SELECT * FROM " + PH.TABLE_NOTE;
    Cursor cursor = mDb.rawQuery(sql, null);
    String[] columns = cursor.getColumnNames();

    String[] projectColumns = {PH.NOTE_ID, PH.EVENT_ID, PH.NOTE_CONTENT};
    for (String column : projectColumns) {
      assertThat("Column not implemented: " + column,
        columns,
        hasItemInArray(column));
      Assert.assertThat("False positives are a bad thing: ",
        columns,
        not(hasItemInArray("anUnlikelyColumn")));
    }
    cursor.close();
  }

  @Test
  public void invitationTable_shouldHaveProperColumns() {
    String sql = "SELECT * FROM " + PH.TABLE_INVITATION;
    Cursor cursor = mDb.rawQuery(sql, null);
    String[] columns = cursor.getColumnNames();

    String[] projectColumns = {PH.INVITATION_ID, PH.EVENT_ID,
      PH.USER_PHONE};
    for (String column : projectColumns) {
      assertThat("Column not implemented: " + column,
        columns,
        hasItemInArray(column));
      Assert.assertThat("False positives are a bad thing: ",
        columns,
        not(hasItemInArray("anUnlikelyColumn")));
    }
    cursor.close();
  }

  @Test
  public void userTable_shouldHaveProperColumns() {
    String sql = "SELECT * FROM " + PH.TABLE_USER;
    Cursor cursor = mDb.rawQuery(sql, null);
    String[] columns = cursor.getColumnNames();

    String[] projectColumns = {PH.USER_PHONE, PH.DISPLAY_NAME,
      PH.PICTURE_SMALL, PH.TIME_ZONE};
    for (String column : projectColumns) {
      assertThat("Column not implemented: " + column,
        columns, hasItemInArray(column));
      Assert.assertThat("False positives are a bad thing: ",
        columns, not(hasItemInArray("anUnlikelyColumn")));
    }
    cursor.close();
  }

  @Test
  public void eventTable_newEventsMayBeAdded() {
    mHelper.saveEvent(testEvent);
    Event retrievedEvent = mHelper.getEvent(testEvent.getEventId());

    assertEquals(testEvent.getEventId(), retrievedEvent.getEventId());
    assertEquals(testEvent.getInvitations(), retrievedEvent.getInvitations());
    assertEquals(testEvent.getFromTime(), retrievedEvent.getFromTime());
    assertEquals(testEvent.getToTime(), retrievedEvent.getToTime());
    assertEquals(testEvent.getLocation(), retrievedEvent.getLocation());
    assertEquals(testEvent.getName(), retrievedEvent.getName());
    assertEquals(testEvent.getNotes(), retrievedEvent.getNotes());
    assertEquals(testEvent.getOwnerId(), retrievedEvent.getOwnerId());
    assertEquals(testEvent.getPictureMedium(), retrievedEvent.getPictureMedium());
    assertEquals(testEvent.getPictureSmall(), retrievedEvent.getPictureSmall());
  }

}


