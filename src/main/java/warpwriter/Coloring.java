package warpwriter;

/**
 * Created by Tommy Ettinger on 11/4/2017.
 */
public class Coloring {
    // values with special meanings
    /** Empty space with nothing in it. */
    public static final byte EMPTY = 0,
    /** Used for shadows on the ground, only if nothing is at that space; does not produce outlines. */ SHADOW = 1,
    /** A solid black outline drawn around the perimeter of a 2D image; never used in 3D. */ OUTLINE = 2,
    /** A solid object that is transparent, so it will have an outline but no color of its own. */ CLEAR = 3;
    // 4, 5, 6, and 7 are reserved for later use.
    // all other bytes are organized so the bottom 3 bits determine shading, from darkest at 0 to lightest at 7, while
    // the top 5 bits are palette-dependent and are used to determine the precise hue and saturation.

    public static final int[] CW_PALETTE = {
            0x00000000, 0x444444ff, 0x000000ff, 0x88ffff00, 0x212121ff, 0x00ff00ff, 0x0000ffff, 0x080808ff,
            0x1f1f1fff, 0x3f3f3fff, 0x5f5f5fff, 0x7f7f7fff, 0x9f9f9fff, 0xbfbfbfff, 0xdfdfdfff, 0xffffffff,
            0xa11616ff, 0x923535ff, 0xbe1111ff, 0xe21414ff, 0xb76363ff, 0xf43b3bff, 0xed8383ff, 0xe4a7a7ff,
            0xa35113ff, 0xa36b41ff, 0xc15b0fff, 0xe56d11ff, 0xcc9b75ff, 0xf68a39ff, 0xf1b283ff, 0xecc7abff,
            0x8a582cff, 0x937358ff, 0xa45e21ff, 0xc37027ff, 0xbea289ff, 0xdf8f47ff, 0xdeba9bff, 0xe2cdbbff,
            0xa8701eff, 0xb18b55ff, 0xc77f17ff, 0xed971bff, 0xddbc8bff, 0xfbaf3fff, 0xf7cd8fff, 0xf3dbb6ff,
            0x9f8810ff, 0xae9f4bff, 0xbca00cff, 0xdfbf0eff, 0xdccf85ff, 0xf2d536ff, 0xf1df81ff, 0xf0e7afff,
            0xa9a915ff, 0xc1c05bff, 0xc9c810ff, 0xeeee13ff, 0xf2f197ff, 0xfcfc3aff, 0xfcfb88ff, 0xfafab7ff,
            0x82a113ff, 0x9db252ff, 0x98be0fff, 0xb5e211ff, 0xcfe28cff, 0xcbf439ff, 0xdbf386ff, 0xe5f2b3ff,
            0x72a10dff, 0x90b049ff, 0x86be0aff, 0x9fe20cff, 0xc2de83ff, 0xb8f435ff, 0xcef27eff, 0xdcf1aeff,
            0x68a62aff, 0x8fb766ff, 0x73c51fff, 0x88ea25ff, 0xc1e59eff, 0xa1f946ff, 0xc9f79cff, 0xdaf5c0ff,
            0x269d16ff, 0x51a347ff, 0x24ba11ff, 0x2bdd14ff, 0x85cd7cff, 0x4ff13aff, 0x92ee86ff, 0xb6ebafff,
            0x269940ff, 0x56a167ff, 0x1cb53fff, 0x21d74bff, 0x8acd9aff, 0x43ec6bff, 0x95eaa9ff, 0xb8eac4ff,
            0x1ea574ff, 0x54af8eff, 0x16c385ff, 0x1ae89eff, 0x8adbbeff, 0x3ff8b6ff, 0x8ff4d0ff, 0xb6f1dcff,
            0x0d9d99ff, 0x41a6a3ff, 0x09bab6ff, 0x0bddd8ff, 0x78d1cfff, 0x34f1ecff, 0x7deeebff, 0xabedebff,
            0x1b819aff, 0x4a8e9eff, 0x1596b6ff, 0x18b1d9ff, 0x7eb9c9ff, 0x3dcaedff, 0x8bd7ebff, 0xb1dee9ff,
            0x16519dff, 0x386094ff, 0x115bbaff, 0x146cddff, 0x688cb9ff, 0x3a8bf1ff, 0x83b0ebff, 0xa9c3e5ff,
            0x131b9cff, 0x262c84ff, 0x0e19b9ff, 0x111edbff, 0x5156a4ff, 0x3844efff, 0x7d83e7ff, 0xa0a3deff,
            0x360a97ff, 0x3b1d7eff, 0x3d08b2ff, 0x4909d4ff, 0x64489fff, 0x6c33eaff, 0x9774e2ff, 0xaf9bdbff,
            0x5e179cff, 0x63328dff, 0x6b11b9ff, 0x8015dbff, 0x8c61b1ff, 0x9c3befff, 0xb983e9ff, 0xc6a7e2ff,
            0x871c9dff, 0x863f95ff, 0x9e15baff, 0xbb19ddff, 0xae6ebbff, 0xd13ef1ff, 0xda89ebff, 0xdcade5ff,
            0x9c117eff, 0x92327dff, 0xb90d94ff, 0xdb0fb0ff, 0xb762a5ff, 0xef37c8ff, 0xea7ed2ff, 0xe4a5d6ff,
            0x9a0e4aff, 0x8c2b55ff, 0xb60b54ff, 0xd90d64ff, 0xb05a7fff, 0xed3585ff, 0xe77aa9ff, 0xe1a2bdff,
            0x000000ff, 0x101010ff, 0x202020ff, 0x303030ff, 0x404040ff, 0x505050ff, 0x606060ff, 0x707070ff,
            0x808080ff, 0x909090ff, 0xa0a0a0ff, 0xb0b0b0ff, 0xc0c0c0ff, 0xd0d0d0ff, 0xe0e0e0ff, 0xf0f0f0ff,
            0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff,
            0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff,
            0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff,
            0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff,
            0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff,
            0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff,
            0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff, 0x000000ff,
    };
    public static final int[] ALT_PALETTE = { // change first item to, say, 0x00FF00FF to make all backgrounds green
            0x00000000, 0x444444FF, 0x000000FF, 0x88FFFF00, 0x212121FF, 0x00FF00FF, 0x0000FFFF, 0x0F0F0FFF,
            0x2B2B2BFF, 0x474747FF, 0x636363FF, 0x7F7F7FFF, 0x9B9B9BFF, 0xB7B7B7FF, 0xD3D3D3FF, 0xEFEFEFFF,
            0x170000FF, 0x370000FF, 0x570000FF, 0x770000FF, 0x970000FF, 0xB70808FF, 0xD71717FF, 0xF72929FF,
            0x17100BFF, 0x37291DFF, 0x574334FF, 0x77604EFF, 0x977F6CFF, 0xB7A08FFF, 0xD7C4B5FF, 0xF7EAE0FF,
            0x170800FF, 0x371600FF, 0x572500FF, 0x773700FF, 0x974B08FF, 0xB76115FF, 0xD77927FF, 0xF7933CFF,
            0x170C00FF, 0x371E00FF, 0x573200FF, 0x774703FF, 0x975E0DFF, 0xB7771CFF, 0xD7912EFF, 0xF7AD44FF,
            0x170F03FF, 0x37250CFF, 0x573D19FF, 0x775729FF, 0x97723EFF, 0xB78F57FF, 0xD7AD73FF, 0xF7CE94FF,
            0x171300FF, 0x372E00FF, 0x574900FF, 0x776503FF, 0x97820DFF, 0xB79F1CFF, 0xD7BD2EFF, 0xF7DB44FF,
            0x171705FF, 0x373710FF, 0x57561FFF, 0x777632FF, 0x979649FF, 0xB7B564FF, 0xD7D583FF, 0xF7F5A6FF,
            0x141708FF, 0x303717FF, 0x4D572AFF, 0x6B7741FF, 0x8A975CFF, 0xAAB77BFF, 0xCAD79EFF, 0xECF7C5FF,
            0x101700FF, 0x283700FF, 0x405700FF, 0x597700FF, 0x739700FF, 0x8EB700FF, 0xAAD70BFF, 0xC6F71CFF,
            0x0D1700FF, 0x213700FF, 0x355700FF, 0x4B7700FF, 0x629700FF, 0x7BB700FF, 0x94D701FF, 0xAFF710FF,
            0x12170DFF, 0x2D3722FF, 0x49573BFF, 0x687759FF, 0x88977AFF, 0xABB79FFF, 0xD0D7C8FF, 0xF6F7F6FF,
            0x001700FF, 0x003700FF, 0x005700FF, 0x007700FF, 0x069700FF, 0x11B700FF, 0x20D709FF, 0x33F71AFF,
            0x001704FF, 0x00370DFF, 0x065719FF, 0x0F7728FF, 0x1D973AFF, 0x2EB74FFF, 0x44D767FF, 0x5EF782FF,
            0x051709FF, 0x103718FF, 0x1F572BFF, 0x317741FF, 0x489759FF, 0x63B775FF, 0x82D794FF, 0xA5F7B6FF,
            0x00170EFF, 0x003722FF, 0x005738FF, 0x08774FFF, 0x149768FF, 0x23B782FF, 0x37D79DFF, 0x4FF7BAFF,
            0x051717FF, 0x0F3736FF, 0x1D5755FF, 0x307775FF, 0x469794FF, 0x61B7B4FF, 0x7FD7D4FF, 0xA1F7F4FF,
            0x081417FF, 0x163037FF, 0x284E57FF, 0x3F6B77FF, 0x598A97FF, 0x77AAB7FF, 0x99CAD7FF, 0xC0EBF7FF,
            0x001117FF, 0x002A37FF, 0x004357FF, 0x005D77FF, 0x007897FF, 0x0393B7FF, 0x11B0D7FF, 0x23CDF7FF,
            0x000617FF, 0x001137FF, 0x001F57FF, 0x002E77FF, 0x004097FF, 0x0854B7FF, 0x176AD7FF, 0x2983F7FF,
            0x0B0D17FF, 0x1E2237FF, 0x353A57FF, 0x515677FF, 0x707597FF, 0x9398B7FF, 0xBABED7FF, 0xE5E7F7FF,
            0x010017FF, 0x050037FF, 0x0B0057FF, 0x150077FF, 0x210097FF, 0x3000B7FF, 0x4200D7FF, 0x560DF7FF,
            0x000000FF, 0x101010FF, 0x202020FF, 0x303030FF, 0x404040FF, 0x505050FF, 0x606060FF, 0x707070FF,
            0x808080FF, 0x909090FF, 0xA0A0A0FF, 0xB0B0B0FF, 0xC0C0C0FF, 0xD0D0D0FF, 0xE0E0E0FF, 0xF0F0F0FF,
            0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF,
            0x170F03FF, 0x37250CFF, 0x573D19FF, 0x775729FF, 0x97723EFF, 0xB78F57FF, 0xD7AD73FF, 0xF7CE94FF,
            0x17100BFF, 0x37291DFF, 0x574334FF, 0x77604EFF, 0x977F6CFF, 0xB7A08FFF, 0xD7C4B5FF, 0xF7EAE0FF,
            0x170800FF, 0x371600FF, 0x572500FF, 0x773700FF, 0x974B08FF, 0xB76115FF, 0xD77927FF, 0xF7933CFF,
            0x170000FF, 0x370000FF, 0x570000FF, 0x770000FF, 0x970000FF, 0xB70808FF, 0xD71717FF, 0xF72929FF,
            0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF,
            0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF,

//            0x00000000, 0x444444FF, 0x000000FF, 0x88FFFF00, 0x212121FF, 0x00FF00FF, 0x0000FFFF, 0x0F0F0FFF,
//            0x2B2B2BFF, 0x474747FF, 0x636363FF, 0x7F7F7FFF, 0x9B9B9BFF, 0xB7B7B7FF, 0xD3D3D3FF, 0xEFEFEFFF,
//            0x170101FF, 0x370707FF, 0x571111FF, 0x771E1EFF, 0x973030FF, 0xB74646FF, 0xD75F5FFF, 0xF77D7DFF,
//            0x170B01FF, 0x371B06FF, 0x572E10FF, 0x77441DFF, 0x975B2EFF, 0xB77544FF, 0xD7915DFF, 0xF7B07AFF,
//            0x170D03FF, 0x37200BFF, 0x573617FF, 0x774D27FF, 0x97673CFF, 0xB78354FF, 0xD7A070FF, 0xF7C090FF,
//            0x170F01FF, 0x372407FF, 0x573B11FF, 0x77541FFF, 0x976E31FF, 0xB78A47FF, 0xD7A761FF, 0xF7C67FFF,
//            0x171401FF, 0x373006FF, 0x574C0FFF, 0x77691CFF, 0x97872DFF, 0xB7A542FF, 0xD7C45BFF, 0xF7E378FF,
//            0x171701FF, 0x373706FF, 0x575710FF, 0x77771DFF, 0x97972EFF, 0xB7B743FF, 0xD7D75DFF, 0xF7F77AFF,
//            0x121701FF, 0x2D3706FF, 0x485710FF, 0x63771DFF, 0x80972FFF, 0x9EB744FF, 0xBCD75DFF, 0xDBF77BFF,
//            0x101701FF, 0x283706FF, 0x40570EFF, 0x5A771BFF, 0x75972CFF, 0x92B741FF, 0xAFD75AFF, 0xCEF777FF,
//            0x0D1702FF, 0x213709FF, 0x365714FF, 0x4E7723FF, 0x679736FF, 0x83B74DFF, 0xA0D768FF, 0xBFF787FF,
//            0x041701FF, 0x0C3707FF, 0x195710FF, 0x28771EFF, 0x3C9730FF, 0x52B745FF, 0x6DD75FFF, 0x8BF77DFF,
//            0x021707FF, 0x093714FF, 0x145724FF, 0x237737FF, 0x36974DFF, 0x4DB766FF, 0x68D782FF, 0x87F7A2FF,
//            0x011710FF, 0x083726FF, 0x12573EFF, 0x207758FF, 0x329773FF, 0x48B78FFF, 0x62D7ADFF, 0x80F7CCFF,
//            0x011717FF, 0x053736FF, 0x0E5755FF, 0x1B7775FF, 0x2C9794FF, 0x41B7B4FF, 0x5AD7D3FF, 0x76F7F3FF,
//            0x021317FF, 0x082E37FF, 0x124957FF, 0x206677FF, 0x328397FF, 0x48A1B7FF, 0x63C0D7FF, 0x81DFF7FF,
//            0x010B17FF, 0x071C37FF, 0x103057FF, 0x1E4577FF, 0x305D97FF, 0x4578B7FF, 0x5F94D7FF, 0x7DB3F7FF,
//            0x010217FF, 0x060A37FF, 0x101557FF, 0x1D2377FF, 0x2F3697FF, 0x444CB7FF, 0x5E66D7FF, 0x7B83F7FF,
//            0x080117FF, 0x150637FF, 0x250E57FF, 0x381B77FF, 0x4E2C97FF, 0x6641B7FF, 0x815AD7FF, 0x9F77F7FF,
//            0x0D0117FF, 0x210737FF, 0x371157FF, 0x4E1F77FF, 0x683197FF, 0x8347B7FF, 0xA060D7FF, 0xBF7EF7FF,
//            0x130217FF, 0x2F0837FF, 0x4B1257FF, 0x672077FF, 0x853297FF, 0xA348B7FF, 0xC263D7FF, 0xE181F7FF,
//            0x170113FF, 0x37062DFF, 0x571048FF, 0x771D64FF, 0x972E81FF, 0xB7439EFF, 0xD75DBDFF, 0xF77ADCFF,
//            0x17010BFF, 0x37061BFF, 0x570F2EFF, 0x771C44FF, 0x972D5BFF, 0xB74275FF, 0xD75B91FF, 0xF778AFFF,
//            0x000000FF, 0x101010FF, 0x202020FF, 0x303030FF, 0x404040FF, 0x505050FF, 0x606060FF, 0x707070FF,
//            0x000000FF, 0x101010FF, 0x202020FF, 0x303030FF, 0x404040FF, 0x505050FF, 0x606060FF, 0x707070FF,
//            0x808080FF, 0x909090FF, 0xA0A0A0FF, 0xB0B0B0FF, 0xC0C0C0FF, 0xD0D0D0FF, 0xE0E0E0FF, 0xF0F0F0FF,
//            0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF,
//            0x171401FF, 0x373006FF, 0x574C0FFF, 0x77691CFF, 0x97872DFF, 0xB7A542FF, 0xD7C45BFF, 0xF7E378FF,
//            0x170B01FF, 0x371B06FF, 0x572E10FF, 0x77441DFF, 0x975B2EFF, 0xB77544FF, 0xD7915DFF, 0xF7B07AFF,
//            0x170D03FF, 0x37200BFF, 0x573617FF, 0x774D27FF, 0x97673CFF, 0xB78354FF, 0xD7A070FF, 0xF7C090FF,
//            0x170101FF, 0x370707FF, 0x571111FF, 0x771E1EFF, 0x973030FF, 0xB74646FF, 0xD75F5FFF, 0xF77D7DFF,
//            0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF, 0x000000FF,

//            0x00000000, 0x444444FF, 0x000000FF, 0x88FFFF00, 0x212121FF, 0x00FF00FF, 0x0000FFFF, 0x0F0F0FFF,
//            0x2B2B2BFF, 0x474747FF, 0x636363FF, 0x7F7F7FFF, 0x9B9B9BFF, 0xB7B7B7FF, 0xD3D3D3FF, 0xEFEFEFFF,
//            0x170000FF, 0x370101FF, 0x570707FF, 0x771212FF, 0x972020FF, 0xB73232FF, 0xD74949FF, 0xF76363FF,
//            0x17110DFF, 0x372B22FF, 0x57473BFF, 0x776558FF, 0x978679FF, 0xB7A99EFF, 0xD7CEC7FF, 0xF7F5F4FF,
//            0x170B00FF, 0x371C04FF, 0x572F0CFF, 0x774519FF, 0x975C29FF, 0xB7763DFF, 0xD79155FF, 0xF7AF71FF,
//            0x170E01FF, 0x372306FF, 0x573A0FFF, 0x77521CFF, 0x976C2DFF, 0xB78742FF, 0xD7A45CFF, 0xF7C379FF,
//            0x171107FF, 0x372914FF, 0x574325FF, 0x775E3BFF, 0x977B54FF, 0xB79A71FF, 0xD7BB93FF, 0xF7DDB8FF,
//            0x171401FF, 0x373006FF, 0x574C0FFF, 0x77691CFF, 0x97872DFF, 0xB7A542FF, 0xD7C45BFF, 0xF7E478FF,
//            0x171708FF, 0x373718FF, 0x57572BFF, 0x777742FF, 0x97965DFF, 0xB7B67CFF, 0xD7D69FFF, 0xF7F6C6FF,
//            0x15170BFF, 0x32371DFF, 0x4F5733FF, 0x6E774EFF, 0x8E976CFF, 0xAEB78EFF, 0xCFD7B5FF, 0xF1F7DFFF,
//            0x121700FF, 0x2B3700FF, 0x455704FF, 0x60770DFF, 0x7C9719FF, 0x98B72AFF, 0xB6D73FFF, 0xD4F758FF,
//            0x0F1700FF, 0x253700FF, 0x3C5700FF, 0x547708FF, 0x6E9713FF, 0x89B723FF, 0xA5D737FF, 0xC2F74EFF,
//            0x13170EFF, 0x2E3726FF, 0x4C5741FF, 0x6B7760FF, 0x8D9783FF, 0xB1B7ABFF, 0xD6D7D6FF, 0xFEF7FFFF,
//            0x001700FF, 0x053700FF, 0x0D5703FF, 0x18770CFF, 0x279718FF, 0x39B729FF, 0x4FD73EFF, 0x69F757FF,
//            0x031707FF, 0x0A3715FF, 0x165725FF, 0x267738FF, 0x3A974FFF, 0x51B769FF, 0x6DD785FF, 0x8DF7A5FF,
//            0x08170CFF, 0x17371EFF, 0x2A5734FF, 0x41774DFF, 0x5C9769FF, 0x7BB788FF, 0x9DD7AAFF, 0xC4F7D0FF,
//            0x011710FF, 0x083726FF, 0x12573EFF, 0x207758FF, 0x329773FF, 0x48B78FFF, 0x62D7ADFF, 0x80F7CCFF,
//            0x081717FF, 0x173736FF, 0x295756FF, 0x407776FF, 0x5B9795FF, 0x79B7B5FF, 0x9CD7D5FF, 0xC2F7F5FF,
//            0x0A1517FF, 0x1C3237FF, 0x325057FF, 0x4B6E77FF, 0x698E97FF, 0x8BAEB7FF, 0xB1CFD7FF, 0xDAF1F7FF,
//            0x001217FF, 0x002C37FF, 0x064757FF, 0x0F6277FF, 0x1D7F97FF, 0x2E9CB7FF, 0x44BAD7FF, 0x5ED8F7FF,
//            0x000A17FF, 0x011937FF, 0x072A57FF, 0x113E77FF, 0x205497FF, 0x326CB7FF, 0x4887D7FF, 0x62A4F7FF,
//            0x0D0E17FF, 0x232537FF, 0x3C3F57FF, 0x5A5D77FF, 0x7C7F97FF, 0xA1A4B7FF, 0xCBCCD7FF, 0xF8F8F7FF,
//            0x050017FF, 0x0F0037FF, 0x1B0057FF, 0x2A0677FF, 0x3C1297FF, 0x5021B7FF, 0x6834D7FF, 0x814CF7FF,
//            0x000000FF, 0x101010FF, 0x202020FF, 0x303030FF, 0x404040FF, 0x505050FF, 0x606060FF, 0x707070FF,
//            0x808080FF, 0x909090FF, 0xA0A0A0FF, 0xB0B0B0FF, 0xC0C0C0FF, 0xD0D0D0FF, 0xE0E0E0FF, 0xF0F0F0FF,
//            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
//            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
//            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
//            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
//            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
//            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
//            0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000, 0x00000000,
    };
}
