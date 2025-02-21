package com.PictureVacuumCleanerBot.service;

public class HelpText {
    static final String HELP_TEXT = """
            This the bot was created to parse e621

            Type /start to see a welcome message

            Type /luckyPool to get a random pool

            Type /enterPool to get a specific pool

            Type /luckyPost to get a random post

            "Type /enterPost to get a specific post

            "Type /tagGuide to get a guide about "How to get specific posts"

            "Type /help to see this message again

            """;
    static final String TAG_GUIDE_How_To_Request = """
            Sexually explicit
            
            Tags should be connected with + sign without spaces. Words from one tag should be connected with _ add - to the tag to remove it from the search. Example query:
            male+happy_sex+-not_furry
            """;
    static final String TAG_GUIDE_Basics = """
            Sexually explicit
            
            Artist(s)? Use their best known alias. If a picture has more than one artist, tag them all, along with collaboration. If you're not sure who the artist is, tag unknown_artist. If the artist wishes to remain anonymous, use anonymous_artist instead
            Rating? Explicit for fully or partially exposed genitalia (penis, pussy, cloaca, sheath, balls, or anus), various sex acts even if no genitalia are visible, high amounts of violence/gore, sexual fluids such as cum or pussy_juice, and extreme sexual fetishes such as scat, watersports, or BDSM
            Safe for anything that can be viewed in public without much uproar: no genitals, no sexual overtones or poses, no realistic violence, or any questionable activity. Questionable for everything in between, such as topless females and suggestive poses
            Copyright? The original series or company a character or game is owned by
            Character? Tag the character's best known name. If not that, their full name
            Body type? anthro, feral, humanoid, taur, anthrofied (pokemorph, digimorph), ponified, feralized
            Species? human, canine, feline, bovine, cervine, equine, lagomorph, rodent, avian, insect, marine (cetacean, shark), scalie
            Sex/gender? male, female, intersex (herm, maleherm, gynomorph, andromorph), ambiguous_gender
            How many? solo, duo, group, zero_pictured
            Clothing? fully_clothed, partially_clothed, skimpy, nude, bottomless, topless, underwear, open_shirt
            Location? inside, outside, bedroom, kitchen, forest
            Perspective? front_view, rear_view, side_view, three-quarter_view, low-angle_view, high-angle_view, worm's-eye_view, bird's-eye_view, first_person_view
            """;
    static final String TAG_GUIDE_Sexually_Explicit = """
            Sexually explicit
            
            Male bits? penis, balls, sheath, knot, erection, half-erect, flaccid, humanoid_penis, equine_penis, tapering_penis, veiny_penis, uncut, circumcised
            Female bits? pussy, clitoris, plump_labia, equine_pussy, canine_pussy
            Other? butt, anus, puffy_anus, gaping_anus, urethra, genital_slit
            Sex act? sex (male/female, female/female, male/male, bisexual), masturbation, handjob, footjob, fellatio, cunnilingus, vaginal_penetration, anal_penetration, threesome, foursome, orgy, gangbang, frottage, tribadism, orgasm, cum_inside
            Position? Common ones: missionary_position, cowgirl_position, reverse_cowgirl_position, from_behind, 69_position, stand_and_carry_position.
            Sexual themes? bondage, domination, rape, rough_sex, happy_sex, presenting, internal, impregnation, bestiality, interspecies, public, exhibitionism
            Fluids? cum, cumshot, precum, pussy_juice, pussy_ejaculation, saliva
            Toys? dildo, vibrator, buttplug, egg_vibrator, strapon, feeldoe
            """;
    static final String TAG_GUIDE_Pose_Activity_Appearance = """
            Pose / Activity / Appearance
                        
            General activity (if any)? walking, running, fighting, sleeping, dancing, eating, kissing, licking
            Posture? standing, bent_over, sitting, crouching, kneeling, all_fours, on_front, on_side, on_back, ass_up (see tag group:pose for full list)
            Body decor? glasses, ring, necklace, bracelet, anklet, tattoo, piercing, collar, hat
            Fur style? mane, chest_tuft, pubes
            Hair? hair, long hair, short hair
            Breasts? breasts (small_breasts, big_breasts, huge_breasts), nipples, under_boob, side_boob, teats
            Limbs? crossed_arms, raised_arms, arms_behind_head, spread_legs, crossed_legs, raised_leg, legs_up, raised_tail, tailwag
            Gaze? looking_at_viewer, looking_back, eye_contact, eyes_closed
            Expression? blush, wink, smile, grin, tongue_out, naughty_face, embarrassed, happy, sad
            """;
    static final String TAG_Information_And_Requests = """
            Quality/medium? 
            sketch, line_art, monochrome, shaded, pencil_(artwork), watercolor, 3D, digital_media_(artwork)
            Picture organization? comic, multiple_scenes, sequence, close-up, portrait, pinup, solo_focus, wallpaper
            Style? toony, detailed, realistic
            Text and languages? english_text, japanese_text, spanish_text, runes, dialogue, speech_bubble, symbol
            Information? translated, partially_translated, unknown_artist_signature, not_furry, bigger version at the source
            Requests? translation_request, source_request, tagme
            Image size? low_res, hi_res, absurd_res, superabsurd_res
            Year of creation? 2016, 2015, and so on
            """;
    static final String TAG_GUIDE_Do_Not_Tag = """
            Do NOT tag
                        
            Subjective tags that express opinions. Common examples include beautiful, sexy, hot, good, crappy and most other adjectives
            """;
}
