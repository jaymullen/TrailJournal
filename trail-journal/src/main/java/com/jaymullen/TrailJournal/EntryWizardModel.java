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
import com.jaymullen.TrailJournal.wizard.model.*;

public class EntryWizardModel extends AbstractWizardModel {
    public EntryWizardModel(Context context) {
        super(context);
    }

    @Override
    protected PageList onNewRootPageList() {
        return new PageList(
                new BranchPage(this, "Post type")
                        .addBranch("Prep",
                                new DatePage(this, "Entry Date")
                                        .setRequired(true),

                                new TitlePage(this, "Entry Title")
                                        .setRequired(true),

//                                new PhotoPage(this, "Veggies")
//                                        .setChoices("Tomatoes", "Lettuce", "Onions", "Pickles",
//                                                "Cucumbers", "Peppers"),

                                new SingleFixedChoicePage(this, "Display in Journal")
                                        .setChoices("Yes", "No")
                                        .setValue("Yes")
                                        .setRequired(true),

                                new BodyPage(this, "Entry Text")
                                        .setRequired(true))

                        .addBranch("Trail",
                                new DatePage(this, "Entry Date")
                                        .setRequired(true),

                                new LocationPage(this, "Location")
                                        .setRequired(true),

                                new SingleFixedChoicePage(this, "Sleeping Location")
                                        .setChoices("Tent", "Shelter", "Hammock",
                                                "Under Stars", "Hotel", "Hostel", "House")
                                        .setValue("Shelter"),

                                new TitlePage(this, "Daily Miles")
                                        .setRequired(true),

//                                new PhotoPage(this, "Veggies")
//                                        .setChoices("Tomatoes", "Lettuce", "Onions", "Pickles",
//                                                "Cucumbers", "Peppers"),

                                new SingleFixedChoicePage(this, "Display in Journal")
                                        .setChoices("Yes", "No")
                                        .setRequired(true),

                                new BodyPage(this, "Entry Text")
                                        .setRequired(true))
                        );
    }
}
