# CitiGrow.ai
tagline

## Given Problem Statement

Education is a fundamental pillar of human development. Yet for many learners in rural India, education does not always translate into relevant skills, practical capabilities, or economic opportunity. While **school enrollment in India reaches 98% for ages 6 to 14**, high attendance masks a severe deficit in foundational learning outcomes.

As students progress into adolescence, the gap between schooling and actual competency widens significantly. **One in four rural youth aged 14 to 18 cannot fluently read a second-grade level text in their regional language, and over 56% cannot solve a simple three-digit by one-digit division problem.**

### Why does this happen? Why is there such a big gap?

One of the reasons and the primary focus of this project: **Teacher Shortages, Absenteeism & Administrative Workload**

+ **Single-Teacher & Multi-grade Classrooms:** Tens of thousands of primary schools operate with a single teacher responsible for simultaneously teaching multiple grades across subjects.

+ **Non-Teaching Duties:** Government school teachers are frequently deployed for non-educational administrative tasks.

## Our Solution

CitiGrow is built to transform rural learning from an isolated struggle into a connected ecosystem of opportunity. Our AI-enabled working prototype brings students, teachers and families together through regional-language audio guidance, personalized learning pathways and career exploration helping bridge the linguistic barriers, extend teacher support and strengthen household participation. It represents an evolution from simply accessing education to building the skills, confidence and support to shape a better future, with the ambition to empower every rural learner to uplift themselves, their family and their community.

### What makes CitiGrow different?

- **Offline-first intelligence, built for rural realities**: Access to guidance should not depend on a reliable internet connection. CitiGrow generates supported speech directly on the device and caches it for reuse, rather than relying on prerecorded narration or cloud speech services. Our approach prioritizes local processing so that language support and essential guidance remain accessible in low-connectivity environments.
  
- **Household empowerment, not just individual learning**: A student’s journey is shaped by the people around them. CitiGrow brings families into that journey through dedicated parent and student experiences that explain learning pathways, potential costs and future opportunities. By helping households understand and support educational choices, we aim to give students the encouragement to pursue new possibilities—and help families break intergenerational cycles of disadvantage and essentially giving the career guidance that is required.
  
- **Start with the learner, not their certificate**: A learner’s class or qualification does not always reflect their current abilities. CitiGrow uses foundational skills checks to understand where support is needed, rather than assuming everyone can begin with the same material. We have develop an ASER-inspired assessment approach that guides learners toward achievable next steps—building confidence through progress, without labelling or leaving anyone behind.
  
### Current Technical Implementation

Currently, we have developed a MVP to lay the foundation of how the product will look like. It incorporates the following features:

+ **Local Text-to-speed**: We have implemented English, Hindi, Tamil and Malayalam, the speech is generated on-device in the application and does not require any internet connectivity. we have used ai4bharat/vits_rasa_13 which has been developed by IIT Madras.
+ **Linking Online Free Courses**: We have linked SWAYAM, DIKSHA and skill India.
+ **Personalization and Profile**: We have a rule based personalization based on the user's interest in the field and his usage of the device.

### Proposed Technical Improvements for the next phase
+ **Linking of Online Free Courses**: Instead of re-directing the user to third-party websites. We will bring those learning courses on our platform. Example implementation: Swayam courses are hosted on YouTube. Instead of redirecting the user to Swayam website and then to YouTube. We would integrate supported course content through permitted embeds or provider partnerships.
  
+ **AI-Powered Skills and Career Mapping**: Instead of a rule-based personalization, we would build a structured map connecting careers, prerequisite skills, assessments and learning resources. The platform will identify the learner’s current skill gaps and recommend achievable next steps, explaining why each resource is relevant. Recommendations will consider affordability, language, available time and location—not interests alone. We would have another model that would be run on cloud to identify niche and non-traditional opportunities that would benefit the students.
  
+ **Linguistic Barrier**: We have already implemented TTS(Text-to-Speech) as proof of concept and for next phase we will implement "Whisper"-locally run speech recognition. This will allow the user to simply speak instead of writing/typing.
  
+ **Offline-First Learning and Synchronization**: Allow learners to access permitted downloadable lessons, practice activities and progress records with limited connectivity. Progress will be stored locally and synchronized when connectivity returns, supporting shared phones and interrupted learning sessions.
  
+ **Teacher-Assisted Learning and Intervention**: Provide teachers with a consent-based cohort dashboard showing progress, recurring difficulties and learners who may need assistance. Teachers will be able to recommend resources and provide targeted support, helping extend their reach without positioning AI as a replacement for teachers.
  
+ **Household Participation and Shared-Device Support**: Give families simple regional-language progress summaries and suggested learning schedules. Strengthen shared-device profiles and privacy controls so households can support learning without accessing a student’s private conversations.
  
+ **Household Participation and Shared-Device Support**
Give families simple regional-language progress summaries and suggested learning schedules. Strengthen shared-device profiles and privacy controls so households can support learning without accessing a student’s private conversations.

+ **Privacy, Security and Scalable Infrastructure**: Introduce secure authentication, organization-level access controls, transactional storage and reliable background synchronization. Collect only necessary information, make sharing explicit and establish appropriate safeguards for minors. These foundations will support expansion across institutions and regions.


### Architecture

<img width="2975" height="3943" alt="mermaid-diagram-2026-10-18-183913" src="https://github.com/user-attachments/assets/836fc233-77e3-4841-8089-dea9da214592" />

### Flow of new student
<img width="1357" height="6174" alt="mermaid-diagram-2026-10-18-192758" src="https://github.com/user-attachments/assets/7e99e604-4de7-45bf-ace8-7b9168511841" />
