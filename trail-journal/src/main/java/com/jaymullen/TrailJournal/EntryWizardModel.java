/*
 * Copyright 2012 Roman Nurik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jaymullen.TrailJournal;

import android.content.Context;
import com.jaymullen.TrailJournal.provider.JournalContract;
import com.jaymullen.TrailJournal.wizard.model.*;

public class EntryWizardModel extends AbstractWizardModel {
    public EntryWizardModel(Context context) {
        super(context);
    }

    @Override
    protected PageList onNewRootPageList() {
        return new PageList(
                new BranchPage(this, "Post type", JournalContract.JournalEntries.TYPE)
                        .addBranch("Prep",
                                new DatePage(this, "Entry Date"),

                                new TitlePage(this, "Entry Title"),

//                                new PhotoPage(this, "Veggies")
//                                        .setChoices("Tomatoes", "Lettuce", "Onions", "Pickles",
//                                                "Cucumbers", "Peppers"),

                                new SingleFixedChoicePage(this, "Display in Journal", JournalContract.JournalEntries.DISPLAY_IN_JOURNAL)
                                        .setChoices("Yes", "No")
                                        .setValue("Yes"),

                                new BodyPage(this, "Entry Text")
                        )
                        .addBranch("Trail",
                                new DatePage(this, "Entry Date"),

                                new LocationPage(this, "Location"),

                                new SingleFixedChoicePage(this, "Sleeping Location", JournalContract.JournalEntries.SLEEP_LOCATION)
                                        .setChoices("Tent", "Shelter", "Hammock",
                                                "Under Stars", "Hotel", "Hostel", "House")
                                        .setValue("Shelter"),

                                new TitlePage(this, "Daily Miles"),

//                                new PhotoPage(this, "Veggies")
//                                        .setChoices("Tomatoes", "Lettuce", "Onions", "Pickles",
//                                                "Cucumbers", "Peppers"),

                                new SingleFixedChoicePage(this, "Display in Journal", JournalContract.JournalEntries.DISPLAY_IN_JOURNAL)
                                        .setChoices("Yes", "No")
                                        .setValue("Yes"),

                                new BodyPage(this, "Entry Text"))
                        );
    }
}
